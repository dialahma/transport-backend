package net.adipappi.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Hello world!
 *
 */
@EnableConfigurationProperties  // 👈 Ajoute cette ligne !
@SpringBootApplication
public class TransportApiApplication
{
    public static void main( String[] args ) { SpringApplication.run(TransportApiApplication.class, args);}
}
