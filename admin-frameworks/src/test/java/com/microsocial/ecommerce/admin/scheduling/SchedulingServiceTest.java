package com.exalt.ecommerce.admin.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private TaskScheduler taskScheduler;
    
    @Mock
    private ScheduledFuture<?> scheduledFuture;
    
    private SchedulingService schedulingService;
    
    @BeforeEach
    void setUp() {
        schedulingService = new SchedulingServiceImpl(taskScheduler);
    }
    
    @Test
    void scheduleAtFixedRate_ShouldScheduleTask() {
        // Given
        Runnable task = () -> System.out.println("Test task");
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(scheduledFuture);
        
        // When
        boolean result = schedulingService.scheduleAtFixedRate("testTask", task, 1000, 5000);
        
        // Then
        assertTrue(result);
        verify(taskScheduler).scheduleAtFixedRate(any(Runnable.class), eq(1000L), eq(5000L));
    }
    
    @Test
    void scheduleWithCron_ShouldScheduleTask() {
        // Given
        Runnable task = () -> System.out.println("Cron task");
        when(taskScheduler.schedule(any(Runnable.class), any(Trigger.class)))
            .thenReturn(scheduledFuture);
        
        // When
        boolean result = schedulingService.scheduleWithCron("cronTask", task, "0 0 * * * *");
        
        // Then
        assertTrue(result);
        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }
    
    @Test
    void cancelTask_ShouldCancelExistingTask() {
        // Given
        Runnable task = () -> System.out.println("Task to cancel");
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(scheduledFuture);
        
        schedulingService.scheduleAtFixedRate("cancelMe", task, 1000, 5000);
        
        // When
        boolean result = schedulingService.cancelTask("cancelMe");
        
        // Then
        assertTrue(result);
        verify(scheduledFuture).cancel(false);
    }
    
    @Test
    void isTaskScheduled_ShouldReturnCorrectStatus() {
        // Given
        Runnable task = () -> System.out.println("Task to check");
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);
        when(scheduledFuture.isCancelled()).thenReturn(false);
        
        schedulingService.scheduleAtFixedRate("checkMe", task, 1000, 5000);
        
        // When
        boolean isScheduled = schedulingService.isTaskScheduled("checkMe");
        
        // Then
        assertTrue(isScheduled);
    }
    
    @Test
    void getNextExecutionTime_ShouldReturnTimeForScheduledTask() {
        // Given
        Runnable task = () -> System.out.println("Task with next execution");
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);
        when(scheduledFuture.isCancelled()).thenReturn(false);
        
        schedulingService.scheduleAtFixedRate("timeTask", task, 1000, 5000);
        
        // When
        LocalDateTime nextExecution = schedulingService.getNextExecutionTime("timeTask");
        
        // Then
        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(LocalDateTime.now()));
    }
    
    @Test
    void rescheduleTask_ShouldUpdateExistingTask() {
        // Given
        Runnable task = () -> System.out.println("Task to reschedule");
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(scheduledFuture);
        
        schedulingService.scheduleAtFixedRate("rescheduleMe", task, 1000, 5000);
        
        // When
        boolean result = schedulingService.rescheduleTask("rescheduleMe", 2000, 10000);
        
        // Then
        assertTrue(result);
        verify(scheduledFuture).cancel(false);
        verify(taskScheduler, times(2)).scheduleAtFixedRate(any(Runnable.class), eq(2000L), eq(10000L));
    }
}
