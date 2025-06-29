package com.exalt.ecosystem.shared.authservice.repository;

import com.exalt.ecosystem.shared.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users with expired passwords
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt IS NOT NULL AND u.passwordExpiresAt < :now")
    List<User> findUsersWithExpiredPasswords(@Param("now") LocalDateTime now);
    
    /**
     * Find locked users
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);
    
    /**
     * Find users that should be unlocked
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    List<User> findUsersToUnlock(@Param("now") LocalDateTime now);
    
    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find enabled users
     */
    List<User> findByEnabledTrue();
    
    /**
     * Find users created after specified date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
}