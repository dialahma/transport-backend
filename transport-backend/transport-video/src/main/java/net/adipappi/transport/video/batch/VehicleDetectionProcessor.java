package net.adipappi.transport.video.batch;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.adipappi.transport.video.service.ObjectDetectionService;

import org.bytedeco.javacv.*;

import org.springframework.stereotype.Component;


import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleDetectionProcessor {

    private final ObjectDetectionService objectDetectionService;

    /**
     * Lit un flux RTSP, détecte les véhicules et applique une fonction sur chaque image annotée.
     *
     * @param rtspUrl                L’URL du flux RTSP (ex : rtsp://user:pass@ip:port/...)
     * @param annotatedFrameConsumer Callback appelé avec chaque image annotée (ex : enregistrement, affichage...)
     */
    public void processRtsp(String rtspUrl, Consumer<byte[]> annotatedFrameConsumer) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setOption("stimeout", "5000000"); // timeout de connexion
    }
}




