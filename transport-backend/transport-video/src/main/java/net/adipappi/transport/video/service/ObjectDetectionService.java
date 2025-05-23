package net.adipappi.transport.video.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;


import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Slf4j
@Service
public class ObjectDetectionService {
    private Net net;
    private List<String> classNames;
    private final float CONFIDENCE_THRESHOLD;
    private final float NMS_THRESHOLD;
    private final Set<Integer> vehicleClasses;

    public ObjectDetectionService() {
    
    	this.CONFIDENCE_THRESHOLD = 0.5f;
    	this.NMS_THRESHOLD = 0.4f;
    	this.vehicleClasses = Set.of(2, 3, 5, 7);

        try {
            loadModel();
            loadClassNames();
            log.info("✅ YOLO model loaded successfully");
        } catch (Exception e) {
            log.error("❌ Error loading YOLO model");
            throw new RuntimeException("Failed to initialize YOLO model", e);
        }
    }

    private void loadModel() throws IOException {
        File cfgFile = extractResourceToTempFile("yolo/yolov4.cfg");
        File weightsFile = extractResourceToTempFile("yolo/yolov4.weights");

        log.info("Loading YOLO model from:");
        log.info("Config: {}", cfgFile.getAbsolutePath());
        log.info("Weights: {}", weightsFile.getAbsolutePath());

        this.net = opencv_dnn.readNetFromDarknet(cfgFile.getAbsolutePath(), weightsFile.getAbsolutePath());
        if (net.empty()) {
            throw new IOException("Failed to load YOLO model");
        }
        net.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
        net.setPreferableTarget(opencv_dnn.DNN_TARGET_CPU);
    }

    private void loadClassNames() throws IOException {
        this.classNames = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream("/yolo/coco.names")) {
            assert in != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    classNames.add(line.trim());
                }
            }
        }
    }

    @PostConstruct
    public void verifyConfiguration() {
        log.info("YOLO Configuration:");
        log.info("- Vehicle classes: {}", vehicleClasses);
        log.info("- Confidence threshold: {}", CONFIDENCE_THRESHOLD);
        log.info("- NMS threshold: {}", NMS_THRESHOLD);

        // Vérification des fichiers YOLO
        try {
            InputStream cfg = getClass().getResourceAsStream("/yolo/yolov4.cfg");
            InputStream weights = getClass().getResourceAsStream("/yolo/yolov4.weights");
            InputStream names = getClass().getResourceAsStream("/yolo/coco.names");

            log.info("YOLO files existence:");
            log.info("- cfg: {}", cfg != null);
            log.info("- weights: {}", weights != null);
            log.info("- names: {}", names != null);

            if (cfg != null) cfg.close();
            if (weights != null) weights.close();
            if (names != null) names.close();
        } catch (Exception e) {
            log.error("Error checking YOLO files: {}", e.getMessage());
        }
    }

    public Mat detectAndAnnotate(Mat image) {
        if (net.empty() || image.empty()) {
            return image.clone();
        }

        try {
            Mat blob = new Mat();
            opencv_dnn.blobFromImage(image, blob, 1.0/255.0,
                    new Size(416, 416),
                    new Scalar(0,0,0,0),
                    true, false, CV_32F);

            net.setInput(blob);
            MatVector outs = getNetworkOutputs();

            List<Rect> boxes = new ArrayList<>();
            List<Float> confidences = new ArrayList<>();
            List<Integer> classIds = new ArrayList<>();

            for (int i = 0; i < outs.size(); i++) {
                processOutput(outs.get(i), image.cols(), image.rows(), boxes, confidences, classIds);
            }

            return applyNMSAndDraw(image.clone(), boxes, confidences, classIds);
        } catch (Exception e) {
            System.err.println("Detection error: " + e.getMessage());
            resetNetwork();
            return image.clone();
        }
    }

    private MatVector getNetworkOutputs() {
        StringVector outNames = net.getUnconnectedOutLayersNames();
        MatVector outs = new MatVector(outNames.size());
        net.forward(outs, outNames);
        return outs;
    }

    private void processOutput(Mat output, int imgWidth, int imgHeight,
                               List<Rect> boxes, List<Float> confidences, List<Integer> classIds) {
        try {
            FloatBuffer floatBuffer = output.createBuffer();
            float[] data = new float[(int)output.total() * output.channels()];
            floatBuffer.get(data);

            int dimensions = output.cols();
            for (int i = 0; i < output.rows(); i++) {
                int rowOffset = i * dimensions;
                if (rowOffset + dimensions > data.length) break;

                float confidence = data[rowOffset + 4];
                if (confidence > CONFIDENCE_THRESHOLD) {
                    int classId = getMaxClassId(data, rowOffset, dimensions);
                    if (vehicleClasses.contains(classId)) {
                        addDetection(data, rowOffset, imgWidth, imgHeight,
                                boxes, confidences, classIds, classId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Output processing error: " + e.getMessage());
        }
    }

    private int getMaxClassId(float[] data, int offset, int dimensions) {
        int classId = 0;
        float maxScore = 0;
        for (int j = 5; j < dimensions; j++) {
            if (data[offset + j] > maxScore) {
                maxScore = data[offset + j];
                classId = j - 5;
            }
        }
        return classId;
    }

    private void addDetection(float[] data, int offset, int width, int height,
                              List<Rect> boxes, List<Float> confidences,
                              List<Integer> classIds, int classId) {
        float centerX = data[offset] * width;
        float centerY = data[offset + 1] * height;
        float boxWidth = data[offset + 2] * width;
        float boxHeight = data[offset + 3] * height;

        boxes.add(new Rect(
                (int)(centerX - boxWidth/2),
                (int)(centerY - boxHeight/2),
                (int)boxWidth,
                (int)boxHeight
        ));
        confidences.add(data[offset + 4]);
        classIds.add(classId);
    }

    private Mat applyNMSAndDraw(Mat image, List<Rect> boxes,
                                List<Float> confidences, List<Integer> classIds) {
        if (boxes.isEmpty()) return image;

        RectVector boxesVec = new RectVector(boxes.size());
        FloatPointer confidencesPtr = new FloatPointer(confidences.size());

        for (int i = 0; i < boxes.size(); i++) {
            boxesVec.put(i, boxes.get(i));
            confidencesPtr.put(i, confidences.get(i));
        }

        IntPointer indices = new IntPointer(confidences.size());
        opencv_dnn.NMSBoxes(boxesVec, confidencesPtr,
                CONFIDENCE_THRESHOLD, NMS_THRESHOLD, indices);

        Mat result = image.clone();
        for (int i = 0; i < indices.limit(); i++) {
            drawDetection(result, boxes, confidences, classIds, indices.get(i));
        }
        return result;
    }

    private void drawDetection(Mat image, List<Rect> boxes,
                               List<Float> confidences, List<Integer> classIds, int idx) {
        Rect box = boxes.get(idx);
        String label = String.format("%s %.2f",
                classNames.get(classIds.get(idx)),
                confidences.get(idx));

        rectangle(image,
                new Point(box.x(), box.y()),
                new Point(box.x() + box.width(), box.y() + box.height()),
                new Scalar(0, 255, 0, 255), 2, LINE_AA, 0);

        putText(image, label,
                new Point(box.x(), box.y() - 5),
                FONT_HERSHEY_SIMPLEX, 0.5,
                new Scalar(0, 255, 0, 255), 1, LINE_AA, false);
    }

    private synchronized void resetNetwork() {
        try {
            net.close();
            loadModel();
            log.info("YOLO network reset");
        } catch (Exception e) {
            log.error("Network reset failed: {}", e.getMessage());
        }
    }

    private File extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/" + resourcePath)) {
            File tempFile = File.createTempFile("yolo-", "-" + new File(resourcePath).getName());
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while (true) {
                    assert in != null;
                    if ((bytesRead = in.read(buffer)) == -1) break;
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        }
    }
}
