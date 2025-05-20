package net.adipappi.transport.video.service;

import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VideoProcessingService {
    private final ExecutorService executorService;

    @Autowired
    private VideoStorageService videoStorageService;

    @Autowired
    private ObjectDetectionService detectionService;

    private final long processingTimeout;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public VideoProcessingService(@Value("${video.processing.timeout}") long processingTimeout) {
        this.processingTimeout = processingTimeout;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void processRtspStream(String rtspUrl) {
        if (isProcessing.compareAndSet(false, true)) {
            executorService.submit(() -> {
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
                    // Ajout des logs de démarrage
                    System.out.println("[VIDEO] Tentative de connexion au flux: " + rtspUrl);

                    configureGrabber(grabber);
                    grabber.start();

                    System.out.println("[VIDEO] Flux connecté. Résolution: "
                            + grabber.getImageWidth() + "x" + grabber.getImageHeight());

                    String outputFile = videoStorageService.generateFilePath(rtspUrl);
                    try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                            outputFile,
                            grabber.getImageWidth(),
                            grabber.getImageHeight())) {

                        System.out.println("[VIDEO] Enregistrement vers: " + outputFile);
                        configureRecorder(recorder, grabber);
                        recorder.start();

                        processFrames(grabber, recorder);
                        System.out.println("[VIDEO] Traitement terminé avec succès");
                    }
                } catch (Exception e) {
                    System.err.println("[VIDEO] Erreur de traitement: " + e.getMessage());
                    e.printStackTrace(); // Stacktrace complète pour le débogage
                }
            });
        } else {
            System.out.println("[VIDEO] Un traitement est déjà en cours");
        }
    }

    private void processWithRetry(String rtspUrl, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                processRtspStream(rtspUrl);
                break;
            } catch (Exception e) {
                attempts++;
                System.err.println("[VIDEO] Tentative " + attempts + "/" + maxRetries + " échouée");
                if (attempts >= maxRetries) {
                    System.err.println("[VIDEO] Abandon après " + maxRetries + " tentatives");
                    throw e;
                }
                try {
                    Thread.sleep(5000); // Attente avant reconnexion
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void configureGrabber(FFmpegFrameGrabber grabber) {
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("stimeout", String.valueOf(processingTimeout));
        grabber.setVideoCodecName("h264");
    }

    private void configureRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber) {
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFrameRate(grabber.getFrameRate());
    }

    private void processFrames(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) throws Exception {
        System.out.println("[VIDEO] Démarrage du traitement des frames...");
        Frame frame;
        int frameCount = 0;

        while ((frame = grabber.grab()) != null) {
            if (frame.image != null) {
                frameCount++;
                if (frameCount % 10 == 0) { // Log toutes les 10 frames
                    System.out.printf("[VIDEO] Traitement frame #%d%n", frameCount);
                }

                Mat mat = FrameUtils.frameToMat(frame);
                Mat processedMat = detectionService.detectAndAnnotate(mat);
                Frame processedFrame = FrameUtils.matToFrame(processedMat);
                recorder.record(processedFrame);
            }
        }
        System.out.println("[VIDEO] Traitement terminé. Frames traitées: " + frameCount);
    }
}
