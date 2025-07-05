package com.gogidix.shared.ecommerce.admin.export.handler.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestData {
    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean active;
    private Date createdAt;
    private LocalDate updatedAt;
    private LocalDateTime lastModified;
    private BigDecimal amount;
    
    // Test data factory method
    public static TestData createSample() {
        TestData data = new TestData();
        data.setId(1L);
        data.setName("Test Product");
        data.setDescription("This is a test product");
        data.setPrice(99.99);
        data.setActive(true);
        data.setCreatedAt(new Date());
        data.setUpdatedAt(LocalDate.now());
        data.setLastModified(LocalDateTime.now());
        data.setAmount(new BigDecimal("1234.56"));
        return data;
    }
}
