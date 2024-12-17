package com.example.backendeventmanagementbooking.repository;

import com.example.backendeventmanagementbooking.domain.entity.EventEntity;
import com.example.backendeventmanagementbooking.domain.entity.UserEntity;
import com.example.backendeventmanagementbooking.enums.EventAccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.UUID;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Page<EventEntity> findAllByUser(Pageable pageable, UserEntity user);

    @Query(value = """
             SELECT * FROM event e WHERE e.access_type = 'PUBLIC' AND start_date >= NOW()
            """, nativeQuery = true)
    Page<EventEntity> findAllPublicEvents(Pageable pageable);


    EventEntity findEventByUuidAndAccessTypeAndStartDateAfter(UUID eventUuid, EventAccessType accessType, Date startDate);

}
