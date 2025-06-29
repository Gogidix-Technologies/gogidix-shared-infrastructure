# Policy Component

The Policy component provides a standardized framework for creating and managing administrative policies across all domains in the Micro-Social-Ecommerce ecosystem.

## Features

- **Extensible Policy Model**: Base classes for creating domain-specific policy implementations
- **Policy Versioning**: Support for managing multiple versions of policies
- **Regional Policy Application**: Apply policies to specific regions or globally
- **Effective Date Management**: Control when policies take effect and expire

## Structure

- **model**: Core policy data models including `BasePolicy` with status tracking and regional application
- **service**: Services for managing policies including creation, updating, and versioning
- **controller**: REST controllers for policy APIs

## How to Use

Extend the base classes to create domain-specific implementations:

```java
public class ShippingPolicy extends BasePolicy {
    
    private int freeShippingThreshold;
    private Map<String, Double> shippingRates;
    private boolean expeditedShippingAvailable;
    
    public ShippingPolicy() {
        super();
        this.shippingRates = new HashMap<>();
    }
    
    @Override
    public boolean validate() {
        // Shipping policy-specific validation logic
        return freeShippingThreshold >= 0 && !shippingRates.isEmpty();
    }
    
    // Getters and setters...
}
```

Implement domain-specific services by extending the base service:

```java
@Service
public class ShippingPolicyService extends AbstractPolicyService<ShippingPolicy> {
    
    @Override
    protected ShippingPolicy applyDomainSpecificRules(ShippingPolicy policy) {
        // Apply any shipping-specific rules
        return policy;
    }
    
    @Override
    protected boolean validateDomainPolicy(ShippingPolicy policy) {
        // Validate shipping policy
        return policy.validate();
    }
    
    // Implementation of other abstract methods...
}
```

Create REST controllers for your domain policies:

```java
@RestController
@RequestMapping("/api/shipping/policies")
public class ShippingPolicyController extends AbstractPolicyController<ShippingPolicy, ShippingPolicyService> {
    
    @Autowired
    public ShippingPolicyController(ShippingPolicyService policyService) {
        super(policyService);
    }
    
    @Override
    protected void registerDomainSpecificEndpoints() {
        // Register any shipping-specific endpoints
    }
}
```
