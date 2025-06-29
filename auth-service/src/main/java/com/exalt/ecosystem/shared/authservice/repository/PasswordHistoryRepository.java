package com.exalt.ecosystem.shared.authservice.repository;

import com.exalt.ecosystem.shared.authservice.entity.PasswordHistory;
import com.exalt.ecosystem.shared.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Password History management
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    
    /**
     * Find password history for user ordered by creation date (newest first)
     */
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find recent password history within specified limit
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findRecentPasswordHistory(@Param("user") User user, 
                                                   org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count password changes for user within specified time period
     */
    @Query("SELECT COUNT(ph) FROM PasswordHistory ph WHERE ph.user = :user AND ph.createdAt >= :since")
    long countPasswordChangesAfter(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * Delete old password history entries for user beyond specified count
     */
    @Query("DELETE FROM PasswordHistory ph WHERE ph.user = :user AND ph.id NOT IN " +
           "(SELECT ph2.id FROM PasswordHistory ph2 WHERE ph2.user = :user ORDER BY ph2.createdAt DESC LIMIT :keepCount)")
    void deleteOldPasswordHistory(@Param("user") User user, @Param("keepCount") int keepCount);
    
    /**
     * Find password history entries older than specified date
     */
    List<PasswordHistory> findByCreatedAtBefore(LocalDateTime cutoffDate);
}