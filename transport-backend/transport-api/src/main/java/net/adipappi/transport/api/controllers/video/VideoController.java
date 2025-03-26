package net.adipappi.transport.api.controllers.video;

import net.adipappi.transport.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/start")
    public String startVideoProcessing(@RequestParam String rtspUrl) {
        videoService.startVideoProcessing(rtspUrl);
        return "Traitement vidéo démarré";
    }

    @PostMapping("/stream")
    public String startStreaming(@RequestParam String rtspUrl, @RequestParam String streamName) {
        videoService.startStreaming(rtspUrl, streamName);
        return "Streaming démarré : http://localhost:8086/live/" + streamName;
    }
}
