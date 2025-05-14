package net.adipappi.transport.video.service;

import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class VideoStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(VideoStreamProcessor.class);

    @Value("${video.storage.path}")
    private final String outputPath;

    @Autowired
    private ObjectDetectionService detectionService;
    @Autowired
    private FrameUtils frameUtils;


    // Injection par constructeur
    public VideoStreamProcessor(
            @Value("${video.storage.path}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void processStream(String rtspUrl) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        FFmpegFrameRecorder recorder = null;

        try {
            // Configure grabber
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("stimeout", "5000000");
            grabber.setOption("analyzeduration", "5000000");
            grabber.setOption("probesize", "5000000");
            grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            grabber.start();

            // Create output directory
            Files.createDirectories(Paths.get(outputPath));

            // Initialize recorder
            String outputFile = outputPath + "/recording_" + System.currentTimeMillis() + ".mp4";
            recorder = new FFmpegFrameRecorder(outputFile,
                    grabber.getImageWidth(), grabber.getImageHeight());

            configureRecorder(recorder, grabber);
            recorder.start();

            Frame frame;
            while ((frame = grabber.grab()) != null) {
                if (frame.image != null) {
                    Mat mat = frameUtils.frameToMat(frame);
                    Mat processedMat = detectionService.detectAndAnnotate(mat);
                    Frame processedFrame = frameUtils.matToFrame(processedMat);
                    recorder.record(processedFrame);
                }
            }
        } catch (Exception e) {
            logger.error("Stream processing error for URL: " + rtspUrl, e);
        } finally {
            closeResources(grabber, recorder);
        }
    }

    private void configureRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber) {
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setPixelFormat(grabber.getPixelFormat());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(2000000); // 2 Mbps
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "23");
    }

    private void closeResources(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) {
        try {
            if (recorder != null) recorder.close();
        } catch (Exception e) {
            logger.error("Error closing recorder", e);
        }
        try {
            if (grabber != null) grabber.close();
        } catch (Exception e) {
            logger.error("Error closing grabber", e);
        }
    }
}
