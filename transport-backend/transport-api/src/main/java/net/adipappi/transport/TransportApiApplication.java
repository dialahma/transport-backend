package net.adipappi.transport;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;


/**
 * Hello world!
 *
 */
@EnableConfigurationProperties  // 👈 Ajoute cette ligne !
@SpringBootApplication
public class TransportApiApplication
{
    public static void main( String[] args ) { SpringApplication.run(TransportApiApplication.class, args);}

    @PostConstruct
    public void init() {
        // Précharge les bibliothèques natives
        Loader.load(avutil.class);
        Loader.load(avcodec.class);

        // Configure le niveau de log
        System.setProperty("org.bytedeco.javacpp.logger", "slf4j");
    }
}
