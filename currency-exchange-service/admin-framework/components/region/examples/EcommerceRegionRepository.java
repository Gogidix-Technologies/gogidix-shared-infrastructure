package com.gogidix.shared.admin.components.region.examples;

import com.microsocial.admin.components.region.model.BaseRegion;
import com.microsocial.admin.components.region.repository.BaseRegionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Example implementation of BaseRegionRepository for e-commerce regions.
 * This interface extends both BaseRegionRepository and JpaRepository for database operations.
 */
@Repository
public interface EcommerceRegionRepository extends BaseRegionRepository<EcommerceRegion>, 
                                                 JpaRepository<EcommerceRegion, String> {
    
    /**
     * Find regions by tax code.
     * 
     * @param taxCode Tax code
     * @return List of regions with the specified tax code
     */
    java.util.List<EcommerceRegion> findByTaxCode(String taxCode);
    
    /**
     * Find regions where shipping is enabled.
     * 
     * @return List of regions with shipping enabled
     */
    java.util.List<EcommerceRegion> findByShippingEnabledTrue();
    
    /**
     * Find regions where international shipping is enabled.
     * 
     * @return List of regions with international shipping enabled
     */
    java.util.List<EcommerceRegion> findByInternationalShippingEnabledTrue();
    
    /**
     * Find regions by default locale.
     * 
     * @param locale Default locale
     * @return List of regions with the specified default locale
     */
    java.util.List<EcommerceRegion> findByDefaultLocale(String locale);
    
    /**
     * Find regions that support a specific currency.
     * 
     * @param currencyCode ISO currency code
     * @return List of regions that support the specified currency
     */
    java.util.List<EcommerceRegion> findBySupportedCurrenciesContaining(String currencyCode);
}
