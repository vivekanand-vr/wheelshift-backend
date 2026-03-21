package com.wheelshiftpro.scheduler;

import com.wheelshiftpro.entity.Reservation;
import com.wheelshiftpro.enums.ReservationStatus;
import com.wheelshiftpro.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that transitions overdue PENDING/CONFIRMED reservations to EXPIRED.
 *
 * Runs every 5 minutes. Protected by ShedLock so that only one node executes
 * the job at a time in a clustered deployment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelayString = "${reservation.expiry.check-interval-ms:300000}")
    @SchedulerLock(name = "ReservationExpiryScheduler", lockAtMostFor = "4m", lockAtLeastFor = "1m")
    @Transactional
    public void expireStaleReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expired = reservationRepository.findExpiredReservations(now);

        if (expired.isEmpty()) {
            log.debug("No expired reservations found at {}", now);
            return;
        }

        log.info("Expiring {} stale reservation(s)", expired.size());

        for (Reservation reservation : expired) {
            log.debug("Expiring reservation id={} (expiryDate={})", reservation.getId(), reservation.getExpiryDate());
            reservation.setStatus(ReservationStatus.EXPIRED);
        }

        log.info("Successfully expired {} reservation(s)", expired.size());
    }
}
