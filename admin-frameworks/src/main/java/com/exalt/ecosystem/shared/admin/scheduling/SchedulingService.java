package com.exalt.ecosystem.shared.admin.scheduling;

import java.time.LocalDateTime;

/**
 * Service interface for managing scheduled tasks in the admin framework.
 * Provides comprehensive task scheduling capabilities including:
 * - Fixed rate scheduling
 * - Cron-based scheduling
 * - Task cancellation and rescheduling
 * - Task status monitoring
 */
public interface SchedulingService {

    /**
     * Schedule a task to run at a fixed rate.
     * 
     * @param taskId Unique identifier for the task
     * @param task The task to execute
     * @param initialDelayMs Initial delay before first execution (in milliseconds)
     * @param fixedRateMs Interval between task executions (in milliseconds)
     * @return true if task was scheduled successfully, false otherwise
     */
    boolean scheduleAtFixedRate(String taskId, Runnable task, long initialDelayMs, long fixedRateMs);

    /**
     * Schedule a task using a cron expression.
     * 
     * @param taskId Unique identifier for the task
     * @param task The task to execute
     * @param cronExpression Cron expression defining when the task should run
     * @return true if task was scheduled successfully, false otherwise
     */
    boolean scheduleWithCron(String taskId, Runnable task, String cronExpression);

    /**
     * Reschedule an existing task with new timing parameters.
     * 
     * @param taskId Unique identifier for the task to reschedule
     * @param initialDelayMs New initial delay (in milliseconds)
     * @param fixedRateMs New interval between executions (in milliseconds)
     * @return true if task was rescheduled successfully, false otherwise
     */
    boolean rescheduleTask(String taskId, long initialDelayMs, long fixedRateMs);

    /**
     * Cancel a scheduled task.
     * 
     * @param taskId Unique identifier for the task to cancel
     * @return true if task was cancelled successfully, false if task was not found
     */
    boolean cancelTask(String taskId);

    /**
     * Get the next execution time for a scheduled task.
     * 
     * @param taskId Unique identifier for the task
     * @return LocalDateTime of next execution, or null if task not found or finished
     */
    LocalDateTime getNextExecutionTime(String taskId);

    /**
     * Check if a task is currently scheduled and active.
     * 
     * @param taskId Unique identifier for the task
     * @return true if task is scheduled and not cancelled/completed, false otherwise
     */
    boolean isTaskScheduled(String taskId);
}
