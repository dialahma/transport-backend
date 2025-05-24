package net.adipappi.transport.video.batch;

import jakarta.annotation.PreDestroy;
import net.adipappi.transport.video.service.ObjectDetectionService;
import net.adipappi.transport.video.util.FrameUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class VideoStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(VideoStreamProcessor.class);
    private final Map<String, Long> activeStreams = new ConcurrentHashMap<>();
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);

    @Value("${video.storage.path}")
    private String outputPath;

    @Autowired
    private ObjectDetectionService detectionService;

    public void processStream(String rtspUrl) {
        String streamId = extractStreamId(rtspUrl);
        activeStreams.put(streamId, System.currentTimeMillis());

        int attempt = 0;
        final int maxRetries = 3;

        while (attempt < maxRetries && !shutdownFlag.get()) {
            try (FFmpegFrameGrabber grabber = createGrabber(rtspUrl)) {
                grabber.start();
                logger.info("Stream started: {}", rtspUrl);

                String outputFile = String.format("%s/recording_%d.mp4",
                        outputPath, System.currentTimeMillis());

                try (FFmpegFrameRecorder recorder = createRecorder(outputFile, grabber)) {
                    recorder.start();
                    processFrames(grabber, recorder, streamId);
                    attempt = 0; // Reset on success
                }
            } catch (Exception e) {
                logger.error("Stream error (attempt {}/{}): {}",
                        ++attempt, maxRetries, e.getMessage());
                sleepBeforeRetry(attempt);
            }
        }

        activeStreams.remove(streamId);
    }

    private FFmpegFrameGrabber createGrabber(String rtspUrl) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("stimeout", "5000000");
        grabber.setOption("max_delay", "5000000");
        grabber.setVideoCodecName("h264");
        return grabber;
    }

    private FFmpegFrameRecorder createRecorder(String outputFile, FFmpegFrameGrabber grabber) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                outputFile,
                grabber.getImageWidth(),
                grabber.getImageHeight()
        );
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("tune", "zerolatency");
        return recorder;
    }

    private void processFrames(FFmpegFrameGrabber grabber,
                               FFmpegFrameRecorder recorder,
                               String streamId) throws Exception {
        Frame frame;
        while (!shutdownFlag.get() && (frame = grabber.grab()) != null) {
            if (frame.image != null) {
                Mat mat = FrameUtils.frameToMat(frame);
                if (!mat.empty()) {
                    Mat processedMat = detectionService.detectAndAnnotate(mat);
                    recorder.record(FrameUtils.matToFrame(processedMat));
                    activeStreams.put(streamId, System.currentTimeMillis());
                }
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void monitorStreams() {
        activeStreams.forEach((streamId, lastActive) -> {
            if (System.currentTimeMillis() - lastActive > 60000) {
                logger.warn("Restarting inactive stream: {}", streamId);
                new Thread(() -> processStream(getUrlFromId(streamId))).start();
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        shutdownFlag.set(true);
        logger.info("Shutting down all streams");
    }

    private String extractStreamId(String rtspUrl) {
        return rtspUrl.substring(rtspUrl.lastIndexOf('/') + 1);
    }

    private String getUrlFromId(String streamId) {
        return "rtsp://wesadmin:PiCaM*_*1187@adipappi.media:8554/" + streamId;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(Math.min(1000 * (1 << attempt), 30000));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}