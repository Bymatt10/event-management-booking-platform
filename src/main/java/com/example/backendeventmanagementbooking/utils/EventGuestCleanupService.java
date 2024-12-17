package com.example.backendeventmanagementbooking.utils;

import com.example.backendeventmanagementbooking.repository.EventGuestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventGuestCleanupService {

    private final EventGuestRepository eventGuestRepository;

//    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cleanUpOldPendingInvitations() {
        var cutoffTime = LocalDateTime.now().minusMinutes(50);
        log.info("Cleaning up pending invitations older than {}", cutoffTime);
        int rowsDeleted = eventGuestRepository.deletePendingInvitationsOlderThan();
        log.info("Deleted {} pending invitations", rowsDeleted);
    }
}
