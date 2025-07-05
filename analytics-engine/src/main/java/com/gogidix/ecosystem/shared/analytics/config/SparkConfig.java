package com.gogidix.ecosystem.shared.analytics.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configuration class for Apache Spark.
 * Sets up Spark configurations for the analytics engine.
 */
@Configuration
@PropertySource("classpath:spark.properties")
public class SparkConfig {

    @Value("${spark.app.name:analytics-engine}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String masterUri;

    @Value("${spark.executor.memory:1g}")
    private String executorMemory;

    @Value("${spark.driver.memory:1g}")
    private String driverMemory;

    @Value("${spark.serializer:org.apache.spark.serializer.KryoSerializer}")
    private String serializer;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setAppName(appName)
                .setMaster(masterUri)
                .set("spark.executor.memory", executorMemory)
                .set("spark.driver.memory", driverMemory)
                .set("spark.serializer", serializer);
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkConf());
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession
                .builder()
                .config(sparkConf())
                .getOrCreate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
} 