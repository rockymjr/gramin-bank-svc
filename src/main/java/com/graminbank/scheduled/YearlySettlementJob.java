package com.graminbank.scheduled;

import com.graminbank.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YearlySettlementJob {

    private final SettlementService settlementService;

    /**
     * Runs every December 31 at 23:59
     * Cron: second minute hour day month day-of-week
     */
    @Scheduled(cron = "0 59 23 31 12 ?")
    public void performYearlySettlement() {
        log.info("Starting yearly settlement process...");

        try {
            settlementService.settleFinancialYear();
            log.info("Yearly settlement completed successfully");
        } catch (Exception e) {
            log.error("Error during yearly settlement: {}", e.getMessage(), e);
        }
    }
}