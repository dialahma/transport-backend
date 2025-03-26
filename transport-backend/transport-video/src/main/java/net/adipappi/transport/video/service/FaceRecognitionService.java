package net.adipappi.transport.video.service;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_dnn.Net;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;


@Service
public class FaceRecognitionService {

    private Net faceNet;

    public FaceRecognitionService() {
        try {
            String modelPath = extractResourceToTempFile("models/nn4.small2.v1.t7");
            faceNet = opencv_dnn.readNetFromTorch(modelPath);
           /* String modelPath = extractResourceToTempFile("models/facenet.pb");
            faceNet = opencv_dnn.readNetFromTensorflow(modelPath);*/
            System.out.println("✅ Modèle FaceNet chargé !");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement FaceNet : " + e.getMessage());
        }
    }

    private String extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Fichier non trouvé : " + resourcePath);

            File temp = File.createTempFile("model", ".pb");
            temp.deleteOnExit();
            Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (temp.length() == 0) throw new IOException("Le fichier est vide : " + resourcePath);
            return temp.getAbsolutePath();
        }
    }
    public List<String> recognizeFaces(Mat image) {
        // Implémentation...
        return List.of();
    }
}
