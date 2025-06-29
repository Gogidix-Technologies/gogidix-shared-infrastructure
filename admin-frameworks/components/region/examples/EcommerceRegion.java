package com.exalt.admin.components.region.examples;

import com.exalt.admin.components.region.model.BaseRegion;

import java.util.HashSet;
import java.util.Set;

/**
 * Example implementation of BaseRegion for e-commerce regions.
 * This class extends BaseRegion and adds e-commerce-specific properties.
 */
public class EcommerceRegion extends BaseRegion {
    
    private boolean shippingEnabled;
    private Set<String> supportedCurrencies;
    private String defaultLocale;
    private String taxCode;
    private boolean internationalShippingEnabled;
    
    /**
     * Default constructor.
     */
    public EcommerceRegion() {
        super();
        this.supportedCurrencies = new HashSet<>();
    }
    
    /**
     * Constructor with essential region parameters.
     * 
     * @param name Region name
     * @param code Region code
     * @param description Region description
     * @param type Region type
     * @param createdBy User who created this region
     * @param defaultLocale Default locale for this region
     * @param taxCode Tax code for this region
     */
    public EcommerceRegion(String name, String code, String description, 
                          RegionType type, String createdBy,
                          String defaultLocale, String taxCode) {
        super(name, code, description, type, createdBy);
        this.supportedCurrencies = new HashSet<>();
        this.defaultLocale = defaultLocale;
        this.taxCode = taxCode;
    }
    
    /**
     * Validate region data.
     * 
     * @return true if region data is valid, false otherwise
     */
    @Override
    public boolean validate() {
        // Basic validation
        if (getName() == null || getName().trim().isEmpty()) {
            return false;
        }
        
        if (getCode() == null || getCode().trim().isEmpty()) {
            return false;
        }
        
        if (getType() == null) {
            return false;
        }
        
        // E-commerce-specific validation
        if (defaultLocale == null || defaultLocale.trim().isEmpty()) {
            return false;
        }
        
        if (taxCode == null || taxCode.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Add a supported currency.
     * 
     * @param currencyCode ISO currency code
     * @return true if the currency was added, false if it was already supported
     */
    public boolean addSupportedCurrency(String currencyCode) {
        return this.supportedCurrencies.add(currencyCode);
    }
    
    /**
     * Remove a supported currency.
     * 
     * @param currencyCode ISO currency code
     * @return true if the currency was removed, false if it wasn't supported
     */
    public boolean removeSupportedCurrency(String currencyCode) {
        return this.supportedCurrencies.remove(currencyCode);
    }
    
    /**
     * Check if a currency is supported.
     * 
     * @param currencyCode ISO currency code
     * @return true if the currency is supported, false otherwise
     */
    public boolean supportsCurrency(String currencyCode) {
        return this.supportedCurrencies.contains(currencyCode);
    }
    
    // Getters and setters
    
    public boolean isShippingEnabled() {
        return shippingEnabled;
    }

    public void setShippingEnabled(boolean shippingEnabled) {
        this.shippingEnabled = shippingEnabled;
    }

    public Set<String> getSupportedCurrencies() {
        return new HashSet<>(supportedCurrencies);
    }

    public void setSupportedCurrencies(Set<String> supportedCurrencies) {
        this.supportedCurrencies = new HashSet<>(supportedCurrencies);
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public boolean isInternationalShippingEnabled() {
        return internationalShippingEnabled;
    }

    public void setInternationalShippingEnabled(boolean internationalShippingEnabled) {
        this.internationalShippingEnabled = internationalShippingEnabled;
    }
}
