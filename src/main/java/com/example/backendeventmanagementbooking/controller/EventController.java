package com.example.backendeventmanagementbooking.controller;

import com.example.backendeventmanagementbooking.domain.dto.request.*;
import com.example.backendeventmanagementbooking.domain.dto.response.EventGuestDto;
import com.example.backendeventmanagementbooking.domain.dto.response.EventResponseDto;
import com.example.backendeventmanagementbooking.service.EventGuestService;
import com.example.backendeventmanagementbooking.service.EventService;
import com.example.backendeventmanagementbooking.utils.GenericResponse;
import com.example.backendeventmanagementbooking.utils.PaginationUtils;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/event/")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventGuestService eventGuestService;

    @PostMapping("create")
    public ResponseEntity<GenericResponse<EventResponseDto>> saveEvent(@RequestBody @Valid EventDto eventDto) {
        return eventService.saveEvent(eventDto).GenerateResponse();
    }

    @GetMapping("list")
    public ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> getAllEvents(@RequestParam(defaultValue = "3") int size, @RequestParam(defaultValue = "0") int page) {
        return eventService.findAllEvents(PageRequest.of(page, size));
    }

    @GetMapping("list/available")
    public ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> getAllAvailableEvents(@RequestParam(defaultValue = "3") int size, @RequestParam(defaultValue = "0") int page) {
        return eventGuestService.searchAllEventsPublic(PageRequest.of(page, size)).GenerateResponse();
    }

    @GetMapping("find/{uuid}")
    public ResponseEntity<GenericResponse<EventResponseDto>> findEventById(@PathVariable UUID uuid) {
        return eventService.findEventById(uuid).GenerateResponse();
    }

    @DeleteMapping("{uuid}")
    public ResponseEntity<GenericResponse<Object>> deleteEvent(@PathVariable UUID uuid) {
        return eventService.deleteEvent(uuid).GenerateResponse();
    }

    @PutMapping("updated/{uuid}")
    public ResponseEntity<GenericResponse<EventResponseDto>> updateEvent(@PathVariable UUID uuid, @RequestBody EventUpdatedDto eventUpdatedDto) {
        return eventService.updateEvent(uuid, eventUpdatedDto).GenerateResponse();
    }

    @PostMapping("guest/subscribe/public/{eventUuid}")
    public ResponseEntity<GenericResponse<EventGuestDto>> subscribeToPublicEvent(@PathVariable UUID eventUuid) throws MessagingException, IOException {
        return eventGuestService.subscribeToPublicEvent(eventUuid).GenerateResponse();
    }

    @PostMapping("guest/invite/private/{eventUuid}/{userId}")
    public ResponseEntity<GenericResponse<Void>> invitePrivateEvent(@PathVariable UUID eventUuid, @PathVariable UUID userId) throws MessagingException, IOException {
        return eventGuestService.inviteToPrivateEvent(eventUuid, userId).GenerateResponse();
    }

    @PostMapping("guest/subscribe/private")
    public ResponseEntity<GenericResponse<EventGuestDto>> subscribeToPrivateEvent(@RequestParam String securityCode) throws MessagingException, IOException {
        return eventGuestService.subscribeToPrivateEvent(securityCode).GenerateResponse();
    }

    @PutMapping("guest/unsubscribe/private")
    public ResponseEntity<GenericResponse<Void>> unsubscribeFromPrivateEvent(@RequestParam UUID eventUuid) {
        return eventGuestService.unsubscribeFromPrivateEvent(eventUuid).GenerateResponse();
    }

    @GetMapping("list/event/subscribe")
    public ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> listAllMyEventSubscriptions(@RequestParam(defaultValue = "3") int size, @RequestParam(defaultValue = "0") int page) {
        return eventGuestService.listAllMyEventSubscriptions(PageRequest.of(page, size));
    }

    @PutMapping("guest/changeDate")
    public ResponseEntity<GenericResponse<EventDto>> changeDate(@RequestParam UUID eventUuid, UpdateDateDto updateDate) {
        return eventService.changeDate(eventUuid, updateDate).GenerateResponse();
    }

    @PutMapping("guest/changeLocation")
    public ResponseEntity<GenericResponse<EventDto>> changeLocation(@RequestParam UUID eventUuid, UpdatedLocationDto updatedLocationDto) {
        return eventService.changeLocation(eventUuid, updatedLocationDto).GenerateResponse();
    }

    @PutMapping("guest/changePrice")
    public ResponseEntity<GenericResponse<EventDto>> changePrice(@RequestParam UUID eventUuid, EventUpdatedDto eventUpdatedDto) {
        return eventService.changePrice(eventUuid, eventUpdatedDto).GenerateResponse();
    }

    @PutMapping("guest/changeCapacity")
    public ResponseEntity<GenericResponse<EventDto>> changeCapacity(@RequestParam UUID eventUuid, UpdateCapacityDto updateCapacityDto) {
        return eventService.changeCapacity(eventUuid, updateCapacityDto).GenerateResponse();
    }
}
