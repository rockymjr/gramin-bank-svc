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
     * Runs every March 31 at 23:59
     * Cron: second minute hour day month day-of-week
     */
    @Scheduled(cron = "0 59 23 31 3 ?", zone = "Asia/Kolkata")
    public void performYearlySettlement() {
        log.info("=== Starting Yearly Settlement Job (March 31) ===");

        try {
            settlementService.settleFinancialYear();
            log.info("=== Yearly Settlement Completed Successfully ===");
        } catch (Exception e) {
            log.error("Error during yearly settlement: {}", e.getMessage(), e);
        }
    }

    // For testing: runs every day at midnight (uncomment for testing)
    // @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    // public void testSettlement() {
    //     log.info("Test settlement triggered");
    //     settlementService.settleFinancialYear();
    // }
}