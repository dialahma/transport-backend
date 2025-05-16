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

@Service
public class VideoProcessingService {
    private final ExecutorService executorService;

    @Autowired
    private VideoStorageService videoStorageService;

    @Autowired
    private ObjectDetectionService detectionService;

    private final long processingTimeout;

    public VideoProcessingService(@Value("${video.processing.timeout}") long processingTimeout) {
        this.processingTimeout = processingTimeout;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void processRtspStream(String rtspUrl) {
        executorService.submit(() -> {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
                configureGrabber(grabber);
                grabber.start();

                String outputFile = videoStorageService.generateFilePath(rtspUrl);
                try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                        outputFile,
                        grabber.getImageWidth(),
                        grabber.getImageHeight())) {

                    configureRecorder(recorder, grabber);
                    recorder.start();

                    processFrames(grabber, recorder);
                }
            } catch (Exception e) {
                System.err.println("Error processing RTSP stream: " + e.getMessage());
            }
        });
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
        Frame frame;
        while ((frame = grabber.grab()) != null) {
            if (frame.image != null) {
                Mat mat = FrameUtils.frameToMat(frame);
                Mat processedMat = detectionService.detectAndAnnotate(mat);
                Frame processedFrame = FrameUtils.matToFrame(processedMat);
                recorder.record(processedFrame);
            }
        }
    }
}
