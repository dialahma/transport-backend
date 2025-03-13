package net.adipappi.transport.security.service;

import net.adipappi.transport.dao.entity.User;
import net.adipappi.transport.dao.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + login));

        // Rôle par défaut
        String role = "USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin()) // Utilisez le champ "login"
                .password(user.getPassword()) // Utilisez le champ "password"
                .roles(role) // Rôle par défaut
                .build();
    }
}