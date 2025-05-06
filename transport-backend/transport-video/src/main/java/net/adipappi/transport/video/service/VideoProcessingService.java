package net.adipappi.transport.video.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.adipappi.transport.config.video.VideoConfig;
import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VideoProcessingService {

    @Autowired
    private VideoConfig videoConfig;

    @Autowired
    private ObjectDetectionService objectDetectionService;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Autowired
    private VideoStorageService videoStorageService;

    private final ExecutorService frameProcessor = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(videoConfig.getUrls().size());

        for (String rtspUrl : videoConfig.getUrls()) {
            executorService.submit(() -> {
                videoStorageService.recordStream(rtspUrl, 10); // Enregistrer 10 minutes par défaut
                processRtspStream(rtspUrl);
            });
        }
    }

    public void processRtspStream(String rtspUrl) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)){
             // Java2DFrameConverter converter = new Java2DFrameConverter()) {

            // Configuration optimisée RTSP
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("probesize", "20000000");
            grabber.setOption("stimeout", "5000000"); // 5s timeout
            grabber.setOption("analyzeduration", "10000000");
            grabber.setVideoCodecName("h264");
            grabber.setVideoOption("threads", "auto");
            grabber.start();
            while (!Thread.interrupted()) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    Thread.sleep(50);  // Évite la surcharge CPU
                    continue;
                }

                // 1. Vérifiez que l'image est valide
                if (frame.image == null || frame.imageWidth <= 0 || frame.imageHeight <= 0) {
                    System.err.println("Frame invalide reçue");
                    continue;
                }

                // 2. Traitement parallèle (optimisation)
                processFrameAsync(frame);

                BufferedImage image = FrameUtils.frameToImage(frame);
                Mat mat = FrameUtils.bufferedImageToMat(image);
                // Détection d'objets
                var detectedObjects = objectDetectionService.detectObjects(mat);
                System.out.println("🔹 Objets détectés : " + detectedObjects);

                // Reconnaissance faciale
                var recognizedFaces = faceRecognitionService.recognizeFaces(mat);
                System.out.println("👤 Visages reconnus : " + recognizedFaces);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de traitement du flux : " + rtspUrl);
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanup() {
        frameProcessor.shutdownNow();
        System.out.println("Nettoyage des ressources vidéo terminé");
    }

    private void processFrameAsync(Frame frame) {
        frameProcessor.submit(() -> {
            try {
                BufferedImage image = FrameUtils.frameToImage(frame);
                Mat mat = FrameUtils.bufferedImageToMat(image);

                objectDetectionService.detectObjects(mat);
                faceRecognitionService.recognizeFaces(mat);

            } catch (Exception e) {
                System.err.println("Échec du traitement du frame: " + e.getMessage());
            }
        });
    }
}
