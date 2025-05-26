package net.adipappi.transport.video.batch;

import lombok.extern.slf4j.Slf4j;
import net.adipappi.transport.video.service.ObjectDetectionService;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VideoDetectionProcessor {

    @Autowired
    private ObjectDetectionService objectDetectionService;

    public void process(String rtspUrl) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
            grabber.setOption("rtsp_transport", "tcp");
            grabber.start();

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            while (true) {
                Frame frame = grabber.grabImage();
                if (frame == null) break;

                Mat mat = converter.convert(frame);
                if (mat != null && !mat.empty()) {
                    Mat result = objectDetectionService.detectAndAnnotate(mat);
                    // Tu peux ici :
                    // - Sauvegarder la frame
                    // - Streamer en websocket
                    // - Stocker dans une file pour le frontend
                }
            }
        } catch (Exception e) {
            log.error("Erreur durant le traitement du flux vidéo : {}", e.getMessage(), e);
        }
    }
}
