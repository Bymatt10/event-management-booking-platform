package com.example.backendeventmanagementbooking.service.Impl;

import com.example.backendeventmanagementbooking.domain.dto.common.BuildEmailDto;
import com.example.backendeventmanagementbooking.domain.dto.request.*;
import com.example.backendeventmanagementbooking.domain.dto.response.EventGuestDto;
import com.example.backendeventmanagementbooking.domain.dto.response.EventResponseDto;
import com.example.backendeventmanagementbooking.domain.entity.CategoryEntity;
import com.example.backendeventmanagementbooking.domain.entity.EventEntity;
import com.example.backendeventmanagementbooking.domain.entity.EventGuestEntity;
import com.example.backendeventmanagementbooking.domain.entity.UserEntity;
import com.example.backendeventmanagementbooking.enums.StatusEvent;
import com.example.backendeventmanagementbooking.exception.CustomException;
import com.example.backendeventmanagementbooking.repository.EventGuestRepository;
import com.example.backendeventmanagementbooking.repository.EventRepository;
import com.example.backendeventmanagementbooking.repository.UserRepository;
import com.example.backendeventmanagementbooking.security.SecurityTools;
import com.example.backendeventmanagementbooking.service.CategoryService;
import com.example.backendeventmanagementbooking.service.EventGuestService;
import com.example.backendeventmanagementbooking.service.EventService;
import com.example.backendeventmanagementbooking.utils.BuildEmail;
import com.example.backendeventmanagementbooking.utils.EmailSender;
import com.example.backendeventmanagementbooking.utils.GenericResponse;
import com.example.backendeventmanagementbooking.utils.PaginationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luigivismara.shortuuid.ShortUuid;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.example.backendeventmanagementbooking.config.ConstantsVariables.DEFAULT_ALPHABET;
import static com.example.backendeventmanagementbooking.config.ConstantsVariables.DEFAULT_ALPHABET_UPPER;
import static com.example.backendeventmanagementbooking.enums.EmailFileNameTemplate.CONFIRMATION_EVENT;
import static com.example.backendeventmanagementbooking.enums.EmailFileNameTemplate.INVITE_PRIVATE_EVENT;
import static com.example.backendeventmanagementbooking.enums.EventAccessType.PRIVATE;
import static com.example.backendeventmanagementbooking.enums.EventAccessType.PUBLIC;
import static com.example.backendeventmanagementbooking.enums.InvitationStatusType.*;
import static com.example.backendeventmanagementbooking.utils.PaginationUtils.pageableRepositoryPaginationDto;

@Service

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventServiceImpl implements EventService, EventGuestService {

    private final EventRepository eventRepository;
    private final EventGuestRepository eventGuestRepository;
    private final CategoryService categoryService;
    private final BuildEmail buildEmail;
    private final ObjectMapper objectMapper;
    private final SecurityTools securityTools;
    private final EmailSender emailSender;
    private final EventCacheService eventCacheService;
    private final UserRepository userRepository;

    @Override
    public GenericResponse<EventResponseDto> saveEvent(EventDto eventDto) {
        var user = securityTools.getCurrentUser();
        var eventEntity = new EventEntity(eventDto, user);
        if (eventDto.getEndDate().before(eventEntity.getStartDate())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        var savedEvent = eventRepository.save(eventEntity);

        var categories = categoryService.saveOrGetCategoryList(eventDto.getCategories(), savedEvent)
                .stream()
                .map(CategoryEntity::getName)
                .toList();

        var response = objectMapper.convertValue(savedEvent, EventResponseDto.class);
        response.setCategories(categories);
        return new GenericResponse<>(HttpStatus.OK, response);
    }

    @Override
    public GenericResponse<Object> deleteEvent(UUID uuid) {
        var eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"));
        eventEntity.setStatus(StatusEvent.INACTIVE);
        var user = securityTools.getCurrentUser();
        if (!user.getUsername().equals(eventEntity.getUser().getUsername())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "You do not have permission to delete this event");
        }

        eventRepository.save(eventEntity);
        return new GenericResponse<>(HttpStatus.OK);
    }

    @Override
    public GenericResponse<EventResponseDto> updateEvent(UUID uuid, EventUpdatedDto eventUpdatedDto) throws CustomException {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"))
                .fromEventUpdateDto(eventUpdatedDto, uuid);

        eventRepository.save(eventEntity);

        var listCategories = categoryService.updateCategoryNameByEvent(eventUpdatedDto.getCategoriesUpdated(), eventEntity);
        var response = objectMapper.convertValue(eventUpdatedDto, EventResponseDto.class);
        response.setCategories(listCategories);
        response.setUuid(uuid);

        return new GenericResponse<>(HttpStatus.OK, response);
    }

    @Override
    public GenericResponse<EventResponseDto> findEventById(UUID uuid) {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found with UUID: " + uuid));

        var categories = categoryService.getCategoryNameByEvent(eventEntity);
        EventResponseDto response = objectMapper.convertValue(eventEntity, EventResponseDto.class);
        response.setCategories(categories);
        return new GenericResponse<>(HttpStatus.OK, response);
    }

    @Override
    public ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> findAllEvents(PageRequest pageRequest) {
        var user = securityTools.getCurrentUser();
        var response = pageableRepositoryPaginationDto(eventRepository.findAllByUser(pageRequest, user));
        var dto = paginationFromEntityToEventResponseDto(response);
        return new GenericResponse<>(HttpStatus.OK, dto).GenerateResponse();
    }


    @Override
    public GenericResponse<EventDto> changeDate(UUID uuid, UpdateDateDto updateDate) {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"));

        eventEntity.setStartDate(updateDate.getStartDate());
        eventEntity.setEndDate(updateDate.getEndDate());
        return new GenericResponse<>(HttpStatus.OK, objectMapper.convertValue(eventEntity, EventDto.class));
    }

    @Override
    public GenericResponse<EventDto> changeLocation(UUID uuid, UpdatedLocationDto updatedLocationDto) {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"));

        eventEntity.setLocation(updatedLocationDto.getLocation());
        return new GenericResponse<>(HttpStatus.OK, objectMapper.convertValue(eventEntity, EventDto.class));
    }

    @Override
    public GenericResponse<EventDto> changePrice(UUID uuid, EventUpdatedDto eventUpdatedDt) {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"));

        eventEntity.setPrice(eventUpdatedDt.getPrice());
        return new GenericResponse<>(HttpStatus.OK, objectMapper.convertValue(eventEntity, EventDto.class));
    }

    @Override
    public GenericResponse<EventDto> changeCapacity(UUID uuid, UpdateCapacityDto updateCapacityDto) {
        EventEntity eventEntity = eventRepository.findById(uuid)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Event not found"));

        eventEntity.setCapacity(updateCapacityDto.getCapacity());
        return new GenericResponse<>(HttpStatus.OK, objectMapper.convertValue(eventEntity, EventDto.class));
    }

    @Override
    public ResponseEntity<GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>>> listAllMyEventSubscriptions(PageRequest pageRequest) {
        var user = securityTools.getCurrentUser();
        var response = pageableRepositoryPaginationDto(eventGuestRepository.findAllByUserAndInvitationStatus(user, ACCEPTED, pageRequest));
        var list = response.getValue().stream().map(eventGuestEntity -> {
            var converted = objectMapper.convertValue(eventGuestEntity.getEvent(), EventResponseDto.class);
            var categories = categoryService.getCategoryNameByEvent(eventGuestEntity.getEvent());
            converted.setCategories(categories);
            return converted;
        }).toList();

        var result = new PaginationUtils.PaginationDto<EventResponseDto>(list, response.getCurrentPage(), response.getTotalPages(), response.getTotalItems());
        return new GenericResponse<>(HttpStatus.OK, result).GenerateResponse();
    }

    @Override
    public GenericResponse<PaginationUtils.PaginationDto<EventResponseDto>> searchAllEventsPublic(PageRequest pageRequest) {
        var response = pageableRepositoryPaginationDto(eventRepository.findAllPublicEvents(pageRequest));
        var dto = paginationFromEntityToEventResponseDto(response);
        return new GenericResponse<>(HttpStatus.OK, dto);
    }

    @Override
    public GenericResponse<EventGuestDto> subscribeToPublicEvent(UUID eventUuid) throws MessagingException, IOException {
        var user = securityTools.getCurrentUser();
        var event = eventRepository.findEventByUuidAndAccessTypeAndStartDateAfter(eventUuid, PUBLIC, new Date());
        if (ObjectUtils.isEmpty(event)) return new GenericResponse<>(HttpStatus.NOT_FOUND);

        if (eventGuestRepository.existsByEventAndUser(event, user)) return new GenericResponse<>(HttpStatus.CONFLICT);

        countParticipantInEvent(event);

        var eventGuest = new EventGuestEntity(
                event,
                user,
                PUBLIC,
                ShortUuid.encode(UUID.randomUUID(), DEFAULT_ALPHABET, 5).toString(),
                ACCEPTED);
        var saved = eventGuestRepository.save(eventGuest);
        var response = new EventGuestDto(saved, ACCEPTED);
        sendEmailConfirmation(event, user);
        return new GenericResponse<>(HttpStatus.OK, response);
    }

    @Override
    public GenericResponse<Void> unsubscribeFromPublicEvent(UUID eventUuid) {
        var user = securityTools.getCurrentUser();
        var event = eventRepository.findById(eventUuid);
        if (event.isEmpty()) return new GenericResponse<>("Event does not exist!", HttpStatus.NOT_FOUND);
        var eventGuest = eventGuestRepository.findByEventAndUserAndInvitationStatus(event.get(), user, ACCEPTED);
        if (eventGuest == null)
            return new GenericResponse<>("User is not subscribe in the current event!", HttpStatus.NOT_FOUND);

        eventGuest.setInvitationStatus(DECLINED);
        eventGuestRepository.save(eventGuest);
        return new GenericResponse<>(HttpStatus.OK);
    }


    @Override
    public GenericResponse<Void> inviteToPrivateEvent(UUID eventUuid, UUID userId) throws MessagingException, IOException {
        var user = userRepository.findById(userId);
        if (user.isEmpty()) return new GenericResponse<>("User does not exist!", HttpStatus.NOT_FOUND);
        var event = eventRepository.findEventByUuidAndAccessTypeAndStartDateAfter(eventUuid, PRIVATE, new Date());
        if (ObjectUtils.isEmpty(event)) return new GenericResponse<>(HttpStatus.NOT_FOUND);

        if (eventGuestRepository.existsByEventAndUserAndInvitationStatus(event, user.get(), PENDING))
            return new GenericResponse<>(HttpStatus.CONFLICT);
        countParticipantInEvent(event);
        var eventGuest = new EventGuestEntity(
                event,
                user.get(),
                PRIVATE,
                ShortUuid.encode(UUID.randomUUID(), DEFAULT_ALPHABET_UPPER, 7).toString(),
                PENDING);
        eventGuestRepository.save(eventGuest);
        eventCacheService.saveInvitationToRedis(eventGuest);
        sendEmailPrivateEvent(event, user.get(), eventGuest.getVerificationCode());
        return new GenericResponse<>(HttpStatus.OK);
    }

    @Override
    public GenericResponse<EventGuestDto> subscribeToPrivateEvent(String securityCode) throws MessagingException, IOException {
        var user = securityTools.getCurrentUser();
        var eventSaveRedis = eventCacheService.getInvitationFromRedis(securityCode);
        var event = eventRepository.findEventByUuidAndAccessTypeAndStartDateAfter(eventSaveRedis.eventUuid(), PRIVATE, new Date());
        if (ObjectUtils.isEmpty(event)) throw new CustomException(HttpStatus.NOT_FOUND);

        if (eventGuestRepository.existsByEventAndUserAndInvitationStatus(event, user, ACCEPTED))
            throw new CustomException(HttpStatus.CONFLICT);
        countParticipantInEvent(event);
        var eventGuest = eventGuestRepository.findByEventAndUserAndInvitationStatus(event, user, PENDING);
        if (eventGuest == null) throw new CustomException(HttpStatus.NOT_FOUND, "Event does not exist!");


        if (!eventGuest.getVerificationCode().equals(securityCode)) {
            throw new CustomException(HttpStatus.NOT_ACCEPTABLE, "Verification code mismatch");
        }
        eventGuest.setInvitationStatus(ACCEPTED);
        var saved = eventGuestRepository.save(eventGuest);
        var response = new EventGuestDto(saved, ACCEPTED);
        eventCacheService.evictInvitationFromRedis(securityCode);
        sendEmailConfirmation(event, user);
        return new GenericResponse<>(HttpStatus.OK, response);
    }

    @Override
    public GenericResponse<Void> unsubscribeFromPrivateEvent(UUID eventUuid) {
        var user = securityTools.getCurrentUser();
        var getEvent = eventRepository.findEventByUuidAndAccessTypeAndStartDateAfter(eventUuid, PRIVATE, new Date());
        var eventGuest = eventGuestRepository.findByEventAndUserAndInvitationStatus(getEvent, user, PENDING);
        if (eventGuest == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "We don't have any event!.");
        }
        eventGuest.setInvitationStatus(DECLINED);
        eventGuestRepository.save(eventGuest);
        return new GenericResponse<>(HttpStatus.OK);
    }

    private PaginationUtils.PaginationDto<EventResponseDto> paginationFromEntityToEventResponseDto(PaginationUtils.PaginationDto<EventEntity> response) {
        var transformation = response.getValue().stream().map(event -> {
            var res = objectMapper.convertValue(event, EventResponseDto.class);
            res.setCategories(categoryService.getCategoryNameByEvent(event));
            return res;
        }).toList();

        return new PaginationUtils.PaginationDto<>(transformation,
                response.getCurrentPage(),
                response.getTotalPages(),
                response.getTotalItems()
        );
    }

    @Async("email")
    protected void sendEmailPrivateEvent(EventEntity event, UserEntity user, String securityCode) throws IOException, MessagingException {
        var template = buildEmail.getTemplateEmail(INVITE_PRIVATE_EVENT,
                new BuildEmailDto("#USERNAME", user.getProfile().getFullName()),
                new BuildEmailDto("#EVENT_NAME", event.getTitle()),
                new BuildEmailDto("#DATE", event.getStartDate().toString()),
                new BuildEmailDto("#LOCATION", event.getLocation()),
                new BuildEmailDto("#SECURITY_CODE", securityCode));

        emailSender.sendMail(new EmailDetailsDto(user.getEmail(), template, "Invitation to " + event.getTitle()));
    }

    @Async("email")
    protected void sendEmailConfirmation(EventEntity event, UserEntity user) throws IOException, MessagingException {
        var template = buildEmail.getTemplateEmail(CONFIRMATION_EVENT,
                new BuildEmailDto("#USERNAME", user.getProfile().getFullName()),
                new BuildEmailDto("#EVENT_NAME", event.getTitle()),
                new BuildEmailDto("#DATE", event.getStartDate().toString()),
                new BuildEmailDto("#LOCATION", event.getLocation()));
        emailSender.sendMail(new EmailDetailsDto(user.getEmail(), template, "Invitation to " + event.getTitle()));
    }

    private void countParticipantInEvent(EventEntity event) {
        var totalParticipant = eventGuestRepository.countByEventAndInvitationStatus(event, ACCEPTED);
        if (event.getCapacity() < totalParticipant) {
            throw new CustomException(HttpStatus.NOT_ACCEPTABLE, "Capacity of event reached max");
        }
    }

}
