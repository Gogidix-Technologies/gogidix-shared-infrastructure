package com.gogidix.shared.admin.components.region.examples;

import com.microsocial.admin.components.region.controller.AbstractRegionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Example implementation of AbstractRegionController for e-commerce regions.
 * This class extends AbstractRegionController and adds e-commerce-specific endpoints.
 */
@RestController
@RequestMapping("/api/ecommerce/regions")
public class EcommerceRegionController extends AbstractRegionController<EcommerceRegion, EcommerceRegionService> {
    
    /**
     * Constructor with required service.
     * 
     * @param regionService Ecommerce region service
     */
    @Autowired
    public EcommerceRegionController(EcommerceRegionService regionService) {
        super(regionService);
    }
    
    /**
     * Extract the user ID from the authentication object.
     * This implementation assumes JWT authentication.
     * 
     * @param authentication Authentication object
     * @return User ID
     */
    @Override
    protected String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            return jwtToken.getToken().getSubject();
        }
        
        // Fallback for other authentication types
        return authentication.getName();
    }
    
    /**
     * Get regions by tax code.
     * 
     * @param taxCode Tax code
     * @return List of regions with the specified tax code
     */
    @GetMapping("/search/taxCode")
    public ResponseEntity<List<EcommerceRegion>> getRegionsByTaxCode(@RequestParam String taxCode) {
        List<EcommerceRegion> regions = regionService.getRegionsByTaxCode(taxCode);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get regions where shipping is enabled.
     * 
     * @return List of regions with shipping enabled
     */
    @GetMapping("/search/shipping-enabled")
    public ResponseEntity<List<EcommerceRegion>> getRegionsWithShippingEnabled() {
        List<EcommerceRegion> regions = regionService.getRegionsWithShippingEnabled();
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get regions where international shipping is enabled.
     * 
     * @return List of regions with international shipping enabled
     */
    @GetMapping("/search/international-shipping-enabled")
    public ResponseEntity<List<EcommerceRegion>> getRegionsWithInternationalShippingEnabled() {
        List<EcommerceRegion> regions = regionService.getRegionsWithInternationalShippingEnabled();
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get regions by default locale.
     * 
     * @param locale Default locale
     * @return List of regions with the specified default locale
     */
    @GetMapping("/search/locale")
    public ResponseEntity<List<EcommerceRegion>> getRegionsByDefaultLocale(@RequestParam String locale) {
        List<EcommerceRegion> regions = regionService.getRegionsByDefaultLocale(locale);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get regions that support a specific currency.
     * 
     * @param currencyCode ISO currency code
     * @return List of regions that support the specified currency
     */
    @GetMapping("/search/currency")
    public ResponseEntity<List<EcommerceRegion>> getRegionsBySupportedCurrency(@RequestParam String currencyCode) {
        List<EcommerceRegion> regions = regionService.getRegionsBySupportedCurrency(currencyCode);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Enable shipping for a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}/enable-shipping")
    public ResponseEntity<EcommerceRegion> enableShipping(@PathVariable String id, Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            EcommerceRegion region = regionService.enableShipping(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Disable shipping for a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}/disable-shipping")
    public ResponseEntity<EcommerceRegion> disableShipping(@PathVariable String id, Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            EcommerceRegion region = regionService.disableShipping(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Enable international shipping for a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}/enable-international-shipping")
    public ResponseEntity<EcommerceRegion> enableInternationalShipping(@PathVariable String id, 
                                                                      Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            EcommerceRegion region = regionService.enableInternationalShipping(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Disable international shipping for a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}/disable-international-shipping")
    public ResponseEntity<EcommerceRegion> disableInternationalShipping(@PathVariable String id, 
                                                                       Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            EcommerceRegion region = regionService.disableInternationalShipping(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update the supported currencies for a region.
     * 
     * @param id Region ID
     * @param currencyCodes Set of ISO currency codes
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}/currencies")
    public ResponseEntity<EcommerceRegion> updateSupportedCurrencies(@PathVariable String id, 
                                                                   @RequestBody Set<String> currencyCodes, 
                                                                   Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            EcommerceRegion region = regionService.updateSupportedCurrencies(id, currencyCodes, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
