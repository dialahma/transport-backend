package net.adipappi.transport.dao.repository;

import net.adipappi.transport.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}