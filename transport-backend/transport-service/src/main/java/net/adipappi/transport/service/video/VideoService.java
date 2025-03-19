package net.adipappi.transport.service.video;

import net.adipappi.transport.video.service.VideoProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    @Autowired
    private VideoProcessingService videoProcessingService;

    public void startVideoProcessing(String rtspUrl) {
        videoProcessingService.processRtspStream(rtspUrl);
    }
}
