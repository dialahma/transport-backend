package net.adipappi.transport.config.config;

import jakarta.annotation.PostConstruct;
import net.adipappi.transport.dao.entity.User;
import net.adipappi.transport.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        // Insérer des données initiales
        User user1 = new User();
        user1.setId(Long.valueOf("1"));
        user1.setName("John Doe");
        user1.setLogin("johndoe");
        user1.setPassword("password123");
        user1.setEmail("john.doe@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setId(Long.valueOf("2"));
        user2.setName("Jane Doe");
        user2.setLogin("janedoe");
        user2.setPassword("password456");
        user2.setEmail("jane.doe@example.com");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        userRepository.save(user1);
        userRepository.save(user2);
    }
}