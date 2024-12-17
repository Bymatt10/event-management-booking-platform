package com.example.backendeventmanagementbooking.repository;

import com.example.backendeventmanagementbooking.domain.entity.EventEntity;
import com.example.backendeventmanagementbooking.domain.entity.EventGuestEntity;
import com.example.backendeventmanagementbooking.domain.entity.UserEntity;
import com.example.backendeventmanagementbooking.enums.InvitationStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EventGuestRepository extends JpaRepository<EventGuestEntity, Long> {
    Boolean existsByEventAndUser(EventEntity event, UserEntity user);

    Boolean existsByEventAndUserAndInvitationStatus(EventEntity event, UserEntity user, InvitationStatusType invitationStatus);

    Integer countByEventAndInvitationStatus(EventEntity event, InvitationStatusType invitationStatus);

    EventGuestEntity findByEventAndUserAndInvitationStatus(EventEntity event, UserEntity user, InvitationStatusType invitationStatus);

    EventGuestEntity findByVerificationCode(String verificationCode);

    Page<EventGuestEntity> findAllByUserAndInvitationStatus(UserEntity user, InvitationStatusType invitationStatus, Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM event_guest
            WHERE invitation_status = 'PENDING'
              AND created_at < NOW() - INTERVAL 50 MINUTE;
            """, nativeQuery = true)
    Integer deletePendingInvitationsOlderThan();
}
