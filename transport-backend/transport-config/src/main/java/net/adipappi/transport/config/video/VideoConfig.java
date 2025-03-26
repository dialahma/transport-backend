package net.adipappi.transport.config.video;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "video.rtsp")
@Setter
@Getter
public class VideoConfig {

    @Value("${video.probesize:10000000}")  // Augmentez à 10MB
    private String probesize;

    @Value("${video.analyzeduration:10000000}")  // Augmentez à 10MB
    private String analyzeduration;

    private List<String> urls;

}
