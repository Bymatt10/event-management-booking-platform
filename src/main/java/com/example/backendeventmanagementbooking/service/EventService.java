package com.example.backendeventmanagementbooking.service;

import com.example.backendeventmanagementbooking.domain.dto.request.*;
import com.example.backendeventmanagementbooking.domain.dto.response.EventResponseDto;
import com.example.backendeventmanagementbooking.utils.GenericResponse;
import com.example.backendeventmanagementbooking.utils.PaginationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface EventService {
    GenericResponse<EventResponseDto> saveEvent(EventDto eventDto);

    GenericResponse<Object> deleteEvent(UUID uuid);

    GenericResponse<EventResponseDto> updateEvent(UUID uuid, EventUpdatedDto eventUpdatedDto);

    GenericResponse<EventResponseDto> findEventById(UUID uuid);

    ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> findAllEvents(PageRequest pageRequest);

    GenericResponse<EventDto> changeDate(UUID uuid, UpdateDateDto updateDate);

    GenericResponse<EventDto> changeLocation(UUID uuid, UpdatedLocationDto updatedLocationDto);

    GenericResponse<EventDto> changePrice(UUID uuid, EventUpdatedDto eventUpdatedDto);

    GenericResponse<EventDto> changeCapacity(UUID uuid, UpdateCapacityDto updateCapacityDto);

}
