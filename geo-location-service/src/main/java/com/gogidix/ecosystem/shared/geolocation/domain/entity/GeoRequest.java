package com.gogidix.ecosystem.shared.geolocation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity class to track geo-location requests for auditing and caching.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class GeoRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String requestType;
    
    @Column(length = 1000)
    private String query;
    
    private Double latitude;
    private Double longitude;
    
    @Column(length = 1000)
    private String result;
    
    @Column(nullable = false)
    private String provider;
    
    @Column(nullable = false)
    private LocalDateTime requestTime;
    
    private Integer responseTimeMs;
    
    @PrePersist
    protected void onCreate() {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}
