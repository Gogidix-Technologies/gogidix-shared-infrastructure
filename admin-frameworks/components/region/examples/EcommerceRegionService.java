package com.exalt.admin.components.region.examples;

import com.exalt.admin.components.region.service.AbstractRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * Example implementation of AbstractRegionService for e-commerce regions.
 * This class extends AbstractRegionService and adds e-commerce-specific functionality.
 */
@Service
@Transactional
public class EcommerceRegionService extends AbstractRegionService<EcommerceRegion> {
    
    private final EcommerceRegionRepository ecommerceRegionRepository;
    
    /**
     * Constructor with required repository.
     * 
     * @param ecommerceRegionRepository Repository for e-commerce regions
     */
    @Autowired
    public EcommerceRegionService(EcommerceRegionRepository ecommerceRegionRepository) {
        this.ecommerceRegionRepository = ecommerceRegionRepository;
        this.regionRepository = ecommerceRegionRepository;
    }
    
    /**
     * Get regions by tax code.
     * 
     * @param taxCode Tax code
     * @return List of regions with the specified tax code
     */
    public List<EcommerceRegion> getRegionsByTaxCode(String taxCode) {
        return ecommerceRegionRepository.findByTaxCode(taxCode);
    }
    
    /**
     * Get regions where shipping is enabled.
     * 
     * @return List of regions with shipping enabled
     */
    public List<EcommerceRegion> getRegionsWithShippingEnabled() {
        return ecommerceRegionRepository.findByShippingEnabledTrue();
    }
    
    /**
     * Get regions where international shipping is enabled.
     * 
     * @return List of regions with international shipping enabled
     */
    public List<EcommerceRegion> getRegionsWithInternationalShippingEnabled() {
        return ecommerceRegionRepository.findByInternationalShippingEnabledTrue();
    }
    
    /**
     * Get regions by default locale.
     * 
     * @param locale Default locale
     * @return List of regions with the specified default locale
     */
    public List<EcommerceRegion> getRegionsByDefaultLocale(String locale) {
        return ecommerceRegionRepository.findByDefaultLocale(locale);
    }
    
    /**
     * Get regions that support a specific currency.
     * 
     * @param currencyCode ISO currency code
     * @return List of regions that support the specified currency
     */
    public List<EcommerceRegion> getRegionsBySupportedCurrency(String currencyCode) {
        return ecommerceRegionRepository.findBySupportedCurrenciesContaining(currencyCode);
    }
    
    /**
     * Enable shipping for a region.
     * 
     * @param id Region ID
     * @param userId ID of the user enabling shipping
     * @return Updated region
     * @throws IllegalArgumentException if the region doesn't exist
     */
    public EcommerceRegion enableShipping(String id, String userId) {
        // Check if region exists
        EcommerceRegion region = getRegionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found: " + id));
        
        // Enable shipping
        region.setShippingEnabled(true);
        region.setUpdatedBy(userId);
        region.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated region
        return ecommerceRegionRepository.save(region);
    }
    
    /**
     * Disable shipping for a region.
     * 
     * @param id Region ID
     * @param userId ID of the user disabling shipping
     * @return Updated region
     * @throws IllegalArgumentException if the region doesn't exist
     */
    public EcommerceRegion disableShipping(String id, String userId) {
        // Check if region exists
        EcommerceRegion region = getRegionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found: " + id));
        
        // Disable shipping
        region.setShippingEnabled(false);
        region.setUpdatedBy(userId);
        region.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated region
        return ecommerceRegionRepository.save(region);
    }
    
    /**
     * Enable international shipping for a region.
     * 
     * @param id Region ID
     * @param userId ID of the user enabling international shipping
     * @return Updated region
     * @throws IllegalArgumentException if the region doesn't exist
     * @throws IllegalStateException if shipping is not enabled
     */
    public EcommerceRegion enableInternationalShipping(String id, String userId) {
        // Check if region exists
        EcommerceRegion region = getRegionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found: " + id));
        
        // Check if shipping is enabled
        if (!region.isShippingEnabled()) {
            throw new IllegalStateException("Cannot enable international shipping when shipping is not enabled");
        }
        
        // Enable international shipping
        region.setInternationalShippingEnabled(true);
        region.setUpdatedBy(userId);
        region.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated region
        return ecommerceRegionRepository.save(region);
    }
    
    /**
     * Disable international shipping for a region.
     * 
     * @param id Region ID
     * @param userId ID of the user disabling international shipping
     * @return Updated region
     * @throws IllegalArgumentException if the region doesn't exist
     */
    public EcommerceRegion disableInternationalShipping(String id, String userId) {
        // Check if region exists
        EcommerceRegion region = getRegionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found: " + id));
        
        // Disable international shipping
        region.setInternationalShippingEnabled(false);
        region.setUpdatedBy(userId);
        region.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated region
        return ecommerceRegionRepository.save(region);
    }
    
    /**
     * Update the supported currencies for a region.
     * 
     * @param id Region ID
     * @param currencyCodes Set of ISO currency codes
     * @param userId ID of the user updating the currencies
     * @return Updated region
     * @throws IllegalArgumentException if the region doesn't exist
     */
    public EcommerceRegion updateSupportedCurrencies(String id, java.util.Set<String> currencyCodes, String userId) {
        // Check if region exists
        EcommerceRegion region = getRegionById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found: " + id));
        
        // Update supported currencies
        region.setSupportedCurrencies(currencyCodes);
        region.setUpdatedBy(userId);
        region.setUpdatedAt(LocalDateTime.now());
        
        // Save the updated region
        return ecommerceRegionRepository.save(region);
    }
    
    /**
     * Pre-process a region before saving.
     * Apply e-commerce-specific processing.
     * 
     * @param region Region to process
     */
    @Override
    protected void preProcessRegion(EcommerceRegion region) {
        // Add default currency for the region based on locale
        if (region.getDefaultLocale() != null && region.getSupportedCurrencies().isEmpty()) {
            String currencyCode = getDefaultCurrencyForLocale(region.getDefaultLocale());
            if (currencyCode != null) {
                region.addSupportedCurrency(currencyCode);
            }
        }
    }
    
    /**
     * Get the default currency code for a locale.
     * 
     * @param locale Locale code
     * @return Currency code, or null if unknown
     */
    private String getDefaultCurrencyForLocale(String locale) {
        Map<String, String> localeToCurrency = new HashMap<>();
        
        // Sample mapping of locales to currencies
        localeToCurrency.put("en_US", "USD");
        localeToCurrency.put("en_GB", "GBP");
        localeToCurrency.put("en_CA", "CAD");
        localeToCurrency.put("en_AU", "AUD");
        localeToCurrency.put("fr_FR", "EUR");
        localeToCurrency.put("de_DE", "EUR");
        localeToCurrency.put("it_IT", "EUR");
        localeToCurrency.put("es_ES", "EUR");
        localeToCurrency.put("ja_JP", "JPY");
        localeToCurrency.put("zh_CN", "CNY");
        
        return localeToCurrency.get(locale);
    }
}
