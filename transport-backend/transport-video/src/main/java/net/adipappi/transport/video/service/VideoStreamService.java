package net.adipappi.transport.video.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class VideoStreamService {
    private static final String HLS_OUTPUT_DIR = "../transport-video/target/hls";
    
    public void startStreaming(String rtspUrl, String streamName) {
        new Thread(() -> {
            try {
                String command = String.format("/usr/bin/ffmpeg -i %s -f mpegts -codec:v mpeg1video -s 640x480 -b:v 800k -r 30 http://localhost:8086/live/%s",
                        rtspUrl, streamName);
                Runtime.getRuntime().exec(command);
                System.out.println("✅ Streaming démarré pour : " + streamName);
            } catch (IOException e) {
                System.err.println("❌ Erreur lors du streaming : " + e.getMessage());
            }
        }).start();
    }

    public void startHlsStreaming(String rtspUrl, String streamName) {
        new Thread(() -> {
            try {
                // 1. Créer le dossier HLS s'il n'existe pas
                File hlsDir = new File(HLS_OUTPUT_DIR);
                if (!hlsDir.exists()) {
                    boolean created = hlsDir.mkdirs();
                    if (!created) {
                        System.err.println("❌ Impossible de créer " + HLS_OUTPUT_DIR);
                        return;
                    }
                }

                // 2. Construire la commande FFmpeg
                String command = String.format(
                        "/usr/bin/ffmpeg -analyzeduration 5000000 -probesize 5000000 " +
                        "-rtsp_transport tcp " +
                        "-i \"%s\" " +
                        "-vf scale=640:360 " + // Force la résolution pour éviter les erreurs "size 0x0"
                        "-c:v libx264 -crf 23 -preset veryfast " +
                        "-c:a aac -ar 44100 -ac 1 -b:a 64k " +
                        "-f hls -hls_time 4 -hls_playlist_type event " +
                        "-hls_segment_filename \"%s/%s_%%03d.ts\" " +
                        "\"%s/%s.m3u8\"",
                        rtspUrl,
                        HLS_OUTPUT_DIR, streamName,
                        HLS_OUTPUT_DIR, streamName
                );

                System.out.println("🎬 Lancement de FFmpeg avec la commande suivante :");
                System.out.println(command);

                // 3. Lancer le processus FFmpeg
                // Process process = Runtime.getRuntime().exec(command);
                ProcessBuilder builder = new ProcessBuilder(
							    "/usr/bin/ffmpeg",
							    "-analyzeduration", "5000000",
							    "-probesize", "5000000",
							    "-rtsp_transport", "tcp",
							    "-i", rtspUrl,
							    "-vf", "scale=640:360",
							    "-c:v", "libx264",
							    "-crf", "23",
							    "-preset", "veryfast",
							    "-c:a", "aac",
							    "-ar", "44100",
							    "-ac", "1",
							    "-b:a", "64k",
							    "-f", "hls",
							    "-hls_time", "4",
							    "-hls_playlist_type", "event",
							    "-hls_segment_filename", "../transport-video/target/hls/stream1_%03d.ts",
							    "../transport-video/target/hls/stream1.m3u8"
							);
		        Process process = builder.start();

                System.out.println("✅ Streaming HLS démarré pour : " + streamName);

                // 4. Lire les logs FFmpeg pour debug
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println("[FFmpeg] " + line);
                        }
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la lecture des logs FFmpeg : " + e.getMessage());
                    }
                }).start();

                // 5. Attendre la fin du processus
                new Thread(() -> {
                    try {
                        int exitCode = process.waitFor();
                        System.out.println("FFmpeg terminé avec code : " + exitCode);
                    } catch (InterruptedException e) {
                        System.err.println("Processus FFmpeg interrompu");
                    }
                }).start();

            } catch (IOException e) {
                System.err.println("❌ Erreur lors du démarrage du streaming HLS : " + e.getMessage());
            }
        }).start();
    }
}
