package net.adipappi.transport.video.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class VideoStorageService {

    private static final String OUTPUT_FOLDER = "videos/";

    public VideoStorageService() {
        // Crée le répertoire si inexistant
        new File(OUTPUT_FOLDER).mkdirs();
    }

    public void recordStream(String rtspUrl, int durationMinutes) {
        new Thread(() -> {
            String outputFilePath = generateFilePath(rtspUrl);
            try {
                saveRtspToDisk(rtspUrl, outputFilePath, durationMinutes);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'enregistrement : " + e.getMessage());
            }
        }).start();
    }

    private void saveRtspToDisk(String rtspUrl, String outputFile, int durationMinutes) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFormat("mp4");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.start();

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < (durationMinutes * 60 * 1000)) {
            Frame frame = grabber.grab();
            if (frame != null) {
                recorder.record(frame);
            }
        }

        recorder.stop();
        grabber.stop();
        System.out.println("✅ Enregistrement terminé : " + outputFile);
    }

    private String generateFilePath(String rtspUrl) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return OUTPUT_FOLDER + "recording_" + timestamp + ".mp4";
    }
}
