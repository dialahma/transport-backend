package net.adipappi.transport.video.service;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.bytedeco.opencv.global.opencv_dnn.blobFromImage;
import static org.bytedeco.opencv.global.opencv_core.CV_32F;

@Service
public class FaceRecognitionService {

    private Net faceNet;
    private final Map<String, float[]> knownFaces = new HashMap<>();

    public FaceRecognitionService() {
        try {
            String modelPath = extractResourceToTempFile("models/nn4.small2.v1.t7");
            faceNet = opencv_dnn.readNetFromTorch(modelPath);
            System.out.println("✅ Modèle FaceNet chargé !");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement FaceNet : " + e.getMessage());
        }
    }

    private String extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Fichier non trouvé : " + resourcePath);

            File temp = File.createTempFile("model", ".t7");
            temp.deleteOnExit();
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (temp.length() == 0) throw new IOException("Le fichier est vide : " + resourcePath);
            return temp.getAbsolutePath();
        }
    }

    public List<String> recognizeFaces(Mat image) {
        // Implémentation...
        return List.of("Alice", "Lucie");
    }

    public void register(String name, Mat faceImage) {
        float[] embedding = getEmbedding(faceImage);
        knownFaces.put(name, embedding);
        System.out.println("👤 Enregistré : " + name);
    }

    public Optional<String> recognize(Mat faceImage) {
        float[] inputEmbedding = getEmbedding(faceImage);
        double minDistance = Double.MAX_VALUE;
        String bestMatch = null;

        for (Map.Entry<String, float[]> entry : knownFaces.entrySet()) {
            double distance = cosineDistance(inputEmbedding, entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = entry.getKey();
            }
        }

        // Seuil de confiance (ajustable)
        return minDistance < 0.6 ? Optional.of(bestMatch) : Optional.empty();
    }

    private float[] getEmbedding(Mat faceImage) {
        Mat inputBlob = blobFromImage(faceImage, 1.0 / 128, new Size(96, 96),
                new Scalar(127.5, 127.5, 127.5, 0.0), true, false, CV_32F);

        faceNet.setInput(inputBlob);
        Mat output = faceNet.forward();

        FloatPointer pointer = new FloatPointer(output.data());
        float[] embedding = new float[(int) output.size(1)];

        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = pointer.get(i);
        }

        return embedding;
    }

    private double cosineDistance(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return 1 - (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}
