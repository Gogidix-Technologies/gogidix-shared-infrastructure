package com.exalt.shared.ecommerce.admin.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for scheduling tasks in the admin framework.
 * Sets up a thread pool for scheduled tasks with configurable pool size.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * Configures and provides a TaskScheduler bean with a thread pool.
     * 
     * @return Configured TaskScheduler instance
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // Set the pool size based on expected concurrent tasks
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("admin-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Provides the SchedulingService implementation.
     * 
     * @param taskScheduler The TaskScheduler to use for scheduling tasks
     * @return Configured SchedulingService instance
     */
    @Bean
    public SchedulingService schedulingService(TaskScheduler taskScheduler) {
        return new SchedulingServiceImpl(taskScheduler);
    }
}
