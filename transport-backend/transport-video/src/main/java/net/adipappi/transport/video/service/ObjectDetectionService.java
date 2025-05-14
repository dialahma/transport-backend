package net.adipappi.transport.video.service;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
public class ObjectDetectionService {

    private Net net;
    private List<String> classNames;
    private final float CONFIDENCE_THRESHOLD = 0.5f;
    private final float NMS_THRESHOLD = 0.4f;

    public ObjectDetectionService(@Value("${yolo.mode:darknet}") String yoloMode) {
        try {
            loadModel();
            loadClassNames();
            System.out.println("✅ YOLO model loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Error loading YOLO model");
            e.printStackTrace();
        }
    }

    private void loadModel() throws IOException {
        File cfgFile = extractResourceToTempFile("yolo/yolov4.cfg");
        File weightsFile = extractResourceToTempFile("yolo/yolov4.weights");

        net = opencv_dnn.readNetFromDarknet(cfgFile.getAbsolutePath(), weightsFile.getAbsolutePath());
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
    }

    public Mat detectAndAnnotate(Mat image) {
        if (net.empty() || image.empty()) {
            return image.clone();
        }

        try {
            // Prepare blob from image
            Mat blob = new Mat();
            opencv_dnn.blobFromImage(image, blob, 1.0/255.0,
                    new Size(416, 416),
                    new Scalar(0,0,0,0),
                    true, false, CV_32F);

            net.setInput(blob);

            // Get output layers
            StringVector outNames = net.getUnconnectedOutLayersNames();
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

            // Apply NMS and draw boxes
            return applyNMSAndDraw(image, boxes, confidences, classIds);

        } catch (Exception e) {
            System.err.println("Detection error: " + e.getMessage());
            return image.clone();
        }
    }

    private void processOutput(Mat output, int imgWidth, int imgHeight,
                               List<Rect> boxes, List<Float> confidences, List<Integer> classIds) {
        float[] data = new float[(int)(output.total() * output.channels())];
        output.data().asBuffer().asFloatBuffer().get(data);

        int rows = output.rows();
        int cols = output.cols();

        for (int j = 0; j < rows; j++) {
            int rowOffset = j * cols;
            float confidence = data[rowOffset + 4];

            if (confidence > CONFIDENCE_THRESHOLD) {
                // Get box coordinates
                float centerX = data[rowOffset] * imgWidth;
                float centerY = data[rowOffset + 1] * imgHeight;
                float width = data[rowOffset + 2] * imgWidth;
                float height = data[rowOffset + 3] * imgHeight;

                // Calculate rectangle coordinates
                int left = (int)(centerX - width / 2);
                int top = (int)(centerY - height / 2);

                boxes.add(new Rect(left, top, (int)width, (int)height));
                confidences.add(confidence);
                classIds.add(0); // Single class (vehicle)
            }
        }
    }

    private Mat applyNMSAndDraw(Mat image, List<Rect> boxes, List<Float> confidences, List<Integer> classIds) {
        if (boxes.isEmpty()) {
            return image;
        }

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

        // Draw results
        Mat result = image.clone();
        for (int i = 0; i < indices.limit(); i++) {
            int idx = indices.get(i);
            Rect box = boxes.get(idx);

            rectangle(result,
                    new Point(box.x(), box.y()),
                    new Point(box.x() + box.width(), box.y() + box.height()),
                    new Scalar(0, 255, 0, 255), 2, LINE_AA, 0);

            putText(result,
                    "Vehicle: " + String.format("%.2f", confidences.get(idx)),
                    new Point(box.x(), box.y() - 5),
                    FONT_HERSHEY_SIMPLEX, 0.5,
                    new Scalar(0, 255, 0, 255), 1, LINE_AA, false);
        }

        return result;
    }

    private File extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }

            File tempFile = File.createTempFile("yolo-", "-" + new File(resourcePath).getName());
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            return tempFile;
        }
    }
}