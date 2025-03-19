package net.adipappi.transport.video.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingService {

    public void processRtspStream(String rtspUrl) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);

        try {
            grabber.start();
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                // Traitement des frames (détection, reconnaissance, etc.)
                System.out.println("Frame capturée");
            }
            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
