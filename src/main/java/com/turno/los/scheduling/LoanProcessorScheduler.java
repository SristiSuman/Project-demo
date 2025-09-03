package com.turno.los.scheduling;

import com.turno.los.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanProcessorScheduler {

    private final LoanService loanService;
    private final ThreadPoolTaskExecutor loanExecutor;

    @Scheduled(fixedDelay = 3000)
    public void schedulePicks() {
        var ids = loanService.pickAppliedLoansBatch(10);
        for (UUID id : ids) {
            loanExecutor.execute(() -> {
                try {
                    loanService.systemProcess(id);
                } catch (Exception e) {
                    log.error("Processing failed for {}", id, e);
                }
            });
        }
    }
}
