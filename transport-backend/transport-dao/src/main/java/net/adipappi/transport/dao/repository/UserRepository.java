package net.adipappi.transport.dao.repository;

import net.adipappi.transport.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login); // Utilisez le champ "login"
}