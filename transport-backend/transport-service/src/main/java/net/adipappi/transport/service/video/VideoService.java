package net.adipappi.transport.service.video;

import net.adipappi.transport.video.service.VideoProcessingService;
import net.adipappi.transport.video.service.VideoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    @Autowired
    private VideoProcessingService videoProcessingService;
    @Autowired
    private VideoStreamService  videoStreamService;

    public void startVideoProcessing(String rtspUrl) {
        videoProcessingService.processRtspStream(rtspUrl);
    }

    public void startStreaming(String rtspUrl, String streamName){
        videoStreamService.startStreaming(rtspUrl, streamName);
    }

    public void startHlsStreaming(String rtspUrl, String streamName){
        videoStreamService.startHlsStreaming(rtspUrl, streamName);
    }
}
