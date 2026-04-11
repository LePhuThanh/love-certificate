package com.phelim.system.love_certificate.scheduler;

import com.phelim.system.love_certificate.constant.SchedulerPropertiesKeys;
import com.phelim.system.love_certificate.service.domain.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

// Trigger event
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = SchedulerPropertiesKeys.MILESTONE_ENABLED,
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class MilestoneScheduler {

    private final MilestoneService milestoneService;
    private final AtomicBoolean running = new AtomicBoolean(false); //false => No jobs are currently running, true => currently has a job running

    @Scheduled(fixedRate = 60000) //Every minute (TEST)
//    @Scheduled(cron = "${scheduler.milestone.cron}", zone = "Asia/Ho_Chi_Minh")
    public void runDaily() {
        log.info("[MilestoneScheduler][runDaily] Start to run the ----------MILESTONE-SCHEDULER---------- job");

        //Avoid overlap
        //If running == false => set to true => run the job, If running == true => do not run (skip)
        if (!running.compareAndSet(false, true)) {
            log.warn("[MilestoneScheduler] Previous job still running, skip");
            return;
        }
        try {
            milestoneService.processRelationshipMilestones();
        } catch (Exception ex) {
            log.error("[MilestoneScheduler][runDaily] Error", ex);
        } finally {
            running.set(false); // must have //After the job is finished => reset the status.
        }
        log.info("[MilestoneScheduler][runDaily] End to run the ----------MILESTONE-SCHEDULER---------- job");
    }
}
