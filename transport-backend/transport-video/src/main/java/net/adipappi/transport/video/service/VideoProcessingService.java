package net.adipappi.transport.video.service;

import jakarta.annotation.PostConstruct;
import net.adipappi.transport.config.video.VideoConfig;
import net.adipappi.transport.video.batch.AnnotatedVideoProcessor;
import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VideoProcessingService {

    @Autowired
    private VideoConfig videoConfig;
    @Autowired
    private VideoStorageService videoStorageService;

    @Autowired
    private AnnotatedVideoProcessor annotatedVideoProcessor;
    @Autowired
    private VideoStreamProcessor streamProcessor;


    private final ExecutorService frameProcessor = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(videoConfig.getUrls().size());

        for (String rtspUrl : videoConfig.getUrls()) {
            streamProcessor.processStream(rtspUrl);
            executorService.submit(() -> {
                videoStorageService.recordStream(rtspUrl, 10); // Enregistrer 10 minutes par défaut
                processRtspStream(rtspUrl);
            });
        }
    }

    public void processRtspStream(String rtspUrl) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
            // Configuration optimisée RTSP
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("stimeout", "10000000"); // 10s timeout
            grabber.setOption("analyzeduration", "10000000");
            grabber.setOption("probesize", "10000000");
            grabber.setVideoCodecName("h264");
            grabber.setImageWidth(1920);
            grabber.setImageHeight(1080);
            grabber.setFrameRate(28);
            grabber.start();

            // Configuration enregistreur
            String outputFile = "videos/recording_" + System.currentTimeMillis() + ".mp4";
            FFmpegFrameRecorder recorder = getFFmpegFrameRecorder(outputFile, grabber);

            while (!Thread.interrupted()) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    Thread.sleep(50);
                    continue;
                }

                if (frame.image == null) {
                    continue;
                }

                try {
                    Mat mat = FrameUtils.frameToMat(frame);
                    Mat annotated = annotatedVideoProcessor.annotateFrame(mat);
                    recorder.record(FrameUtils.matToFrame(annotated));
                } catch (Exception e) {
                    System.err.println("❌ Erreur de traitement de frame: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de traitement du flux : " + rtspUrl);
            e.printStackTrace();
        }
    }

    private static FFmpegFrameRecorder getFFmpegFrameRecorder(String outputFile, FFmpegFrameGrabber grabber) throws FFmpegFrameRecorder.Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                outputFile,
                grabber.getImageWidth(),
                grabber.getImageHeight()
        );
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoBitrate(400000); // 400 kb/s
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.start();
        return recorder;
    }

}
