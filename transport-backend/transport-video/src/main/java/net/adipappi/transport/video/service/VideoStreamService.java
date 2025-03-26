package net.adipappi.transport.video.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VideoStreamService {

    public void startStreaming(String rtspUrl, String streamName) {
        new Thread(() -> {
            try {
                String command = String.format("ffmpeg -i %s -f mpegts -codec:v mpeg1video -s 640x480 -b:v 800k -r 30 http://localhost:8086/live/%s",
                        rtspUrl, streamName);
                Runtime.getRuntime().exec(command);
                System.out.println("✅ Streaming démarré pour : " + streamName);
            } catch (IOException e) {
                System.err.println("❌ Erreur lors du streaming : " + e.getMessage());
            }
        }).start();
    }
}
