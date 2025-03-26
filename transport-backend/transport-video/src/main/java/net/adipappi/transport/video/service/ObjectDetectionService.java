package net.adipappi.transport.video.service;

import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.springframework.stereotype.Service;

import java.io.*;


@Service
public class ObjectDetectionService {

    private Net net;

    public ObjectDetectionService() {
        try {
            File cfgFile = extractResourceToTempFile("yolo/yolov4.cfg");
            File weightsFile = extractResourceToTempFile("yolo/yolov4.weights");

            if (cfgFile.length() == 0 || weightsFile.length() == 0) {
                System.err.println("⚠️ YOLO non initialisé : fichiers vides.");
                return;
            }

            net = opencv_dnn.readNetFromDarknet(cfgFile.getAbsolutePath(), weightsFile.getAbsolutePath());


            net.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
            net.setPreferableTarget(opencv_dnn.DNN_TARGET_CPU);

            System.out.println("✅ YOLO model chargé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de YOLO");
            e.printStackTrace();
        }
    }

    private File extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Fichier non trouvé : " + resourcePath);
            }

            File tempFile = File.createTempFile("yolo-", "-" + new File(resourcePath).getName());
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            if (tempFile.length() == 0) {
                throw new IOException("Le fichier est vide : " + resourcePath);
            }

            return tempFile;
        }
    }


    public String detectObjects(Mat image) {
        // Implémenter la détection YOLO sur l'image
        return "Véhicule détecté";
    }

}
