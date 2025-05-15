package net.adipappi.transport.video.service;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class ObjectDetectionService {

    private Net net;
    private List<String> classNames;
    private final float CONFIDENCE_THRESHOLD = 0.5f;
    private final float NMS_THRESHOLD = 0.4f;
    private final Set<Integer> vehicleClasses = Set.of(2, 3, 5, 7); // IDs COCO pour les véhicules
    private final Map<String, Long> lastDetectionTimes = new ConcurrentHashMap<>();

    public ObjectDetectionService(@Value("${yolo.mode:darknet}") String yoloMode) {
        try {
            loadModel();
            loadClassNames();
            System.out.println("✅ YOLO model loaded successfully " + yoloMode);
        } catch (Exception e) {
            System.err.println("❌ Error loading YOLO model");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize YOLO model", e);
        }
    }

    private void loadModel() throws IOException {
        File cfgFile = extractResourceToTempFile("yolo/yolov4.cfg");
        File weightsFile = extractResourceToTempFile("yolo/yolov4.weights");

        System.out.println("Loading YOLO model from:");
        System.out.println("Config: " + cfgFile.getAbsolutePath() + " (" + cfgFile.length() + " bytes)");
        System.out.println("Weights: " + weightsFile.getAbsolutePath() + " (" + weightsFile.length() + " bytes)");

        net = opencv_dnn.readNetFromDarknet(cfgFile.getAbsolutePath(), weightsFile.getAbsolutePath());
        if (net.empty()) {
            throw new IOException("Failed to load YOLO model - network is empty");
        }
        net.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
        net.setPreferableTarget(opencv_dnn.DNN_TARGET_CPU);
    }

    private void loadClassNames() throws IOException {
        classNames = new ArrayList<>();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("yolo/coco.names");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                classNames.add(line.trim());
            }
        }
        if (classNames.isEmpty()) {
            throw new IOException("No class names loaded");
        }
    }

    public Mat detectAndAnnotate(Mat image) {
        if (net.empty() || image.empty()) {
            System.err.println("Network not initialized or empty image");
            return image.clone();
        }

        try {
            // Convert image to blob
            Mat blob = new Mat();
            opencv_dnn.blobFromImage(image, blob, 1.0/255.0,
                    new Size(416, 416),
                    new Scalar(0,0,0,0),
                    true, false, CV_32F);

            net.setInput(blob);

            // Get output layer names
            StringVector outNames = net.getUnconnectedOutLayersNames();
            if (outNames.size() == 0) {
                throw new Exception("No output layers found in the network");
            }

            // Run forward pass
            MatVector outs = new MatVector(outNames.size());
            net.forward(outs, outNames);

            // Process outputs
            List<Rect> boxes = new ArrayList<>();
            List<Float> confidences = new ArrayList<>();
            List<Integer> classIds = new ArrayList<>();

            for (int i = 0; i < outs.size(); i++) {
                Mat output = outs.get(i);
                processOutput(output, image.cols(), image.rows(), boxes, confidences, classIds);
            }

            return applyNMSAndDraw(image.clone(), boxes, confidences, classIds);

        } catch (Exception e) {
            System.err.println("Detection error: " + e.getMessage());
            return image.clone();
        }
    }

    private void processOutput(Mat output, int imgWidth, int imgHeight,
                               List<Rect> boxes, List<Float> confidences, List<Integer> classIds) {
        if (output.empty() || output.rows() == 0) {
            System.err.println("Empty output or no detections");
            return;
        }

        try {
            FloatBuffer floatBuffer = output.createBuffer();
            float[] data = new float[(int)output.total() * output.channels()];
            floatBuffer.get(data);

            int dimensions = output.cols(); // Should be 85 for YOLOv4

            for (int i = 0; i < output.rows(); i++) {
                int rowOffset = i * dimensions;
                if (rowOffset + dimensions > data.length) break;

                float confidence = data[rowOffset + 4];
                if (confidence > CONFIDENCE_THRESHOLD) {
                    // Find class with maximum score
                    int classId = 0;
                    float maxScore = 0;
                    for (int j = 5; j < dimensions; j++) {
                        float score = data[rowOffset + j];
                        if (score > maxScore) {
                            maxScore = score;
                            classId = j - 5;
                        }
                    }

                    // Filter only vehicles
                    if (vehicleClasses.contains(classId)) {
                        float centerX = data[rowOffset] * imgWidth;
                        float centerY = data[rowOffset + 1] * imgHeight;
                        float width = data[rowOffset + 2] * imgWidth;
                        float height = data[rowOffset + 3] * imgHeight;

                        int left = (int)(centerX - width / 2);
                        int top = (int)(centerY - height / 2);

                        boxes.add(new Rect(left, top, (int)width, (int)height));
                        confidences.add(confidence * maxScore);
                        classIds.add(classId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing output: " + e.getMessage());
        }
    }

    private Mat applyNMSAndDraw(Mat image, List<Rect> boxes, List<Float> confidences, List<Integer> classIds) {
        if (boxes.isEmpty()) {
            return image;
        }

        try {
            // Convert to OpenCV format
            RectVector boxesVec = new RectVector(boxes.size());
            FloatPointer confidencesPtr = new FloatPointer(confidences.size());

            for (int i = 0; i < boxes.size(); i++) {
                boxesVec.put(i, boxes.get(i));
                confidencesPtr.put(i, confidences.get(i));
            }

            // Apply NMS
            IntPointer indices = new IntPointer(confidences.size());
            opencv_dnn.NMSBoxes(boxesVec, confidencesPtr, CONFIDENCE_THRESHOLD, NMS_THRESHOLD, indices);

            // Draw results with class labels
            Mat result = image.clone();
            for (int i = 0; i < indices.limit(); i++) {
                int idx = indices.get(i);
                Rect box = boxes.get(idx);
                int classId = classIds.get(idx);
                String label = classNames.get(classId) + " " + String.format("%.2f", confidences.get(idx));

                rectangle(result,
                        new Point(box.x(), box.y()),
                        new Point(box.x() + box.width(), box.y() + box.height()),
                        new Scalar(0, 255, 0, 255), 2, LINE_AA, 0);

                putText(result, label,
                        new Point(box.x(), box.y() - 5),
                        FONT_HERSHEY_SIMPLEX, 0.5,
                        new Scalar(0, 255, 0, 255), 1, LINE_AA, false);
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error drawing detections: " + e.getMessage());
            return image;
        }
    }

    private File extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }

            File tempFile = File.createTempFile("yolo-", "-" + new File(resourcePath).getName());
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        }
    }

    // Méthode pour éviter les boucles infinies
    public boolean isDetectionStale(String streamId) {
        Long lastDetection = lastDetectionTimes.get(streamId);
        if (lastDetection == null) return false;
        return (System.currentTimeMillis() - lastDetection) > 30000; // 30s sans détection
    }

    public void updateLastDetection(String streamId) {
        lastDetectionTimes.put(streamId, System.currentTimeMillis());
    }
}