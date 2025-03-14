package net.adipappi.transport.config.data;

import jakarta.annotation.PostConstruct;
import net.adipappi.transport.dao.entity.User;
import net.adipappi.transport.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injectez PasswordEncoder

    @PostConstruct
    public void init() {
        // Insérer des données initiales avec des mots de passe hachés
        User user1 = new User();
        user1.setId(Long.valueOf("1"));
        user1.setName("John Doe");
        user1.setLogin("johndoe");
        user1.setPassword(passwordEncoder.encode("password123")); // Hacher le mot de passe
        user1.setEmail("john.doe@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setId(Long.valueOf("2"));
        user2.setName("Jane Doe");
        user2.setLogin("janedoe");
        user2.setPassword(passwordEncoder.encode("password456")); // Hacher le mot de passe
        user2.setEmail("jane.doe@example.com");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        userRepository.save(user1);
        userRepository.save(user2);
    }
}
