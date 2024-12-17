package com.example.backendeventmanagementbooking.service;

import com.example.backendeventmanagementbooking.domain.dto.response.EventGuestDto;
import com.example.backendeventmanagementbooking.domain.dto.response.EventResponseDto;
import com.example.backendeventmanagementbooking.utils.GenericResponse;
import com.example.backendeventmanagementbooking.utils.PaginationUtils;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface EventGuestService {

   ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> listAllMyEventSubscriptions(PageRequest pageRequest);


    GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>> searchAllEventsPublic(PageRequest pageRequest);

    GenericResponse<EventGuestDto> subscribeToPublicEvent(UUID eventUuid) throws MessagingException, IOException;

    GenericResponse<Void> unsubscribeFromPublicEvent(UUID eventUuid);

    GenericResponse<Void> inviteToPrivateEvent(UUID eventUuid, UUID userId) throws MessagingException, IOException;

    GenericResponse<EventGuestDto> subscribeToPrivateEvent(String securityCode) throws MessagingException, IOException;

    GenericResponse<Void> unsubscribeFromPrivateEvent(UUID eventUuid);
}
