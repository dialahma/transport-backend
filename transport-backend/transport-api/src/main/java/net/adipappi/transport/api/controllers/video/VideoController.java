package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;

import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Value("${video.hls.output-dir:../transport-video/target/hls}")
    private String hlsOutputDir;

    @PostMapping("/start")
    public String startVideoProcessing(@RequestParam String rtspUrl) {
        videoService.startVideoProcessing(rtspUrl);
        return "Traitement vidéo démarré";
    }

    @PostMapping("/stream")
    public String startStreaming(
            @RequestParam String rtspUrl,
            @RequestParam String streamName,
            @RequestParam(required = false) Boolean hls) {

        if (hls != null && hls) {
            videoService.startHlsStreaming(rtspUrl, streamName);
            return "Streaming HLS démarré : http://localhost:8085/api/video/" + streamName + ".m3u8";
        } else {
            videoService.startStreaming(rtspUrl, streamName);
            return "Streaming MJPEG démarré : http://localhost:8085/api/video/live/" + streamName;
        }
    }


    @GetMapping(value = "/hls/{filename:.+}")
    public ResponseEntity<Resource> serveHlsFile(@PathVariable String filename) {
        Path filePath = Paths.get(hlsOutputDir, filename)
                .normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                MediaType mediaType = filename.endsWith(".m3u8")
                        ? MediaType.parseMediaType("application/vnd.apple.mpegurl")
                        : MediaType.parseMediaType("video/MP2T");

                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache())
                        .contentType(mediaType)
                        .body(resource);
            } else {
                System.err.println("Fichier introuvable: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.err.println("Erreur de chemin: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}

