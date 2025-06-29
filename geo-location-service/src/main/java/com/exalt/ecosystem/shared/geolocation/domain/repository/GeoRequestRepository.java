package com.exalt.ecosystem.shared.geolocation.domain.repository;

import com.exalt.ecosystem.shared.geolocation.domain.entity.GeoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GeoRequest entities.
 */
@Repository
public interface GeoRequestRepository extends JpaRepository<GeoRequest, Long> {
    /**
     * Find the most recent request by query string that has a result.
     * @param query The query string
     * @param requestType The type of request
     * @param since The time threshold
     * @return Optional GeoRequest if found
     */
    Optional<GeoRequest> findFirstByQueryAndRequestTypeAndRequestTimeGreaterThanAndResultIsNotNullOrderByRequestTimeDesc(
            String query, String requestType, LocalDateTime since);
    
    /**
     * Find requests by location coordinates within a time period.
     * @param latitude The latitude
     * @param longitude The longitude
     * @param requestType The type of request
     * @param since The time threshold
     * @return List of matching GeoRequests
     */
    List<GeoRequest> findByLatitudeAndLongitudeAndRequestTypeAndRequestTimeGreaterThanOrderByRequestTimeDesc(
            Double latitude, Double longitude, String requestType, LocalDateTime since);
    
    /**
     * Count requests by provider in a time period.
     * @param provider The provider name
     * @param start The start time
     * @param end The end time
     * @return Count of requests
     */
    long countByProviderAndRequestTimeBetween(String provider, LocalDateTime start, LocalDateTime end);
}
