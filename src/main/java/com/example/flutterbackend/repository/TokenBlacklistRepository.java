package com.example.flutterbackend.repository;

import com.example.flutterbackend.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    // Check if token exists in blacklist
    boolean existsByToken(String token);
    
    // Find by token
    TokenBlacklist findByToken(String token);
    
    // Find all expired tokens
    @Query("SELECT t FROM TokenBlacklist t WHERE t.expiryDate < :now")
    List<TokenBlacklist> findAllExpired(@Param("now") Date now);
    
    // Delete all expired tokens
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiryDate < :now")
    void deleteAllExpired(@Param("now") Date now);
}
