package com.gogidix.ecosystem.shared.admin.scheduling;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Implementation of the SchedulingService interface.
 * Provides centralized task scheduling capabilities for the admin framework.
 */
@Service
public class SchedulingServiceImpl implements SchedulingService {

    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledTaskInfo> taskInfoMap = new ConcurrentHashMap<>();

    public SchedulingServiceImpl(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Override
    public boolean scheduleAtFixedRate(String taskId, Runnable task, long initialDelayMs, long fixedRateMs) {
        if (taskId == null || task == null || fixedRateMs <= 0) {
            return false;
        }

        // Cancel existing task if it exists
        cancelTask(taskId);

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
            wrapTask(taskId, task, false),
            new Date(System.currentTimeMillis() + initialDelayMs),
            fixedRateMs
        );

        scheduledTasks.put(taskId, future);
        taskInfoMap.put(taskId, new ScheduledTaskInfo("FIXED_RATE", initialDelayMs, fixedRateMs, null));
        
        return true;
    }

    @Override
    public boolean scheduleWithCron(String taskId, Runnable task, String cronExpression) {
        if (taskId == null || task == null || cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }

        // Cancel existing task if it exists
        cancelTask(taskId);

        ScheduledFuture<?> future = taskScheduler.schedule(
            wrapTask(taskId, task, true),
            new CronTrigger(cronExpression)
        );

        scheduledTasks.put(taskId, future);
        taskInfoMap.put(taskId, new ScheduledTaskInfo("CRON", 0, 0, cronExpression));
        
        return true;
    }

    @Override
    public boolean rescheduleTask(String taskId, long initialDelayMs, long fixedRateMs) {
        if (taskId == null || fixedRateMs <= 0) {
            return false;
        }

        ScheduledTaskInfo taskInfo = taskInfoMap.get(taskId);
        if (taskInfo == null) {
            return false;
        }

        // Cancel the existing task
        cancelTask(taskId);


        Runnable task = taskInfo.getTask();
        if (task == null) {
            return false;
        }

        // Reschedule with new parameters
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
            wrapTask(taskId, task, false),
            new Date(System.currentTimeMillis() + initialDelayMs),
            fixedRateMs
        );

        scheduledTasks.put(taskId, future);
        taskInfoMap.put(taskId, new ScheduledTaskInfo("FIXED_RATE", initialDelayMs, fixedRateMs, null));
        
        return true;
    }

    @Override
    public boolean cancelTask(String taskId) {
        if (taskId == null) {
            return false;
        }

        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            taskInfoMap.remove(taskId);
            return true;
        }
        return false;
    }

    @Override
    public LocalDateTime getNextExecutionTime(String taskId) {
        if (taskId == null) {
            return null;
        }

        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future == null || future.isDone() || future.isCancelled()) {
            return null;
        }

        // This is a simplified implementation - in a real scenario, you'd need to calculate
        // the next execution time based on the task's schedule
        return LocalDateTime.now().plusSeconds(30); // Placeholder
    }

    @Override
    public boolean isTaskScheduled(String taskId) {
        if (taskId == null) {
            return false;
        }
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        return future != null && !future.isDone() && !future.isCancelled();
    }

    private Runnable wrapTask(String taskId, Runnable task, boolean isCron) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                // Log the error but don't let it propagate to the scheduler
                // In a real implementation, you'd want to log this to your monitoring system
                System.err.println("Error executing scheduled task " + taskId + ": " + e.getMessage());
            }
        };
    }

    /**
     * Internal class to store task information
     */
    private static class ScheduledTaskInfo {
        private final String type;
        private final long initialDelayMs;
        private final long fixedRateMs;
        private final String cronExpression;
        private final Runnable task;

        public ScheduledTaskInfo(String type, long initialDelayMs, long fixedRateMs, String cronExpression) {
            this.type = type;
            this.initialDelayMs = initialDelayMs;
            this.fixedRateMs = fixedRateMs;
            this.cronExpression = cronExpression;
            this.task = null; // In a real implementation, you'd store the original task
        }

        public Runnable getTask() {
            return task;
        }
    }
}
