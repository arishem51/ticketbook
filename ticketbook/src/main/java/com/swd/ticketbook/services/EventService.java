package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.admin.EventResponse;
import com.swd.ticketbook.dto.event.PublicEventResponse;
import com.swd.ticketbook.dto.organizer.*;
import com.swd.ticketbook.entities.*;
import com.swd.ticketbook.enums.EventStatus;
import com.swd.ticketbook.enums.UserRole;
import com.swd.ticketbook.exceptions.BusinessRuleViolationException;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Event operations by Organizers (UC-03.1, UC-03.2)
 * Business Rules: FR2, FR3, FR15, FR17, FR26
 */
@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventUpdateRequestRepository updateRequestRepository;

    @Autowired
    private AuditLogService auditLogService;

    // ==================== EVENT CREATION (UC-03.1) ====================

    /**
     * UC-03.1: Create event
     * FR2: Auto-generate unique event ID
     * FR17, FR26: Only Verified Organizers can create events
     */
    @Transactional
    public EventResponse createEvent(Long organizerId, CreateEventRequest request) {
        // Validate user is Verified Organizer
        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (organizer.getRole() != UserRole.VERIFIED_ORGANIZER) {
            throw new BusinessRuleViolationException("Only Verified Organizers can create events");
        }

        // Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleViolationException("Start date must be before end date");
        }

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleViolationException("Event date must be in the future");
        }

        // Validate at least one ticket type with price > 0
        if (request.getTicketTypes() == null || request.getTicketTypes().isEmpty()) {
            throw new BusinessRuleViolationException("At least one ticket type is required");
        }

        for (TicketTypeRequest tt : request.getTicketTypes()) {
            if (tt.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleViolationException("Ticket price must be greater than 0");
            }
        }

        // Create event
        Event event = new Event(organizer, request.getName(), request.getStartDate(), request.getEndDate());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setLocation(request.getLocation());
        event.setVenueName(request.getVenueName());
        event.setMaxTicketQuantity(request.getMaxTicketQuantity());
        event.setRefundAllowed(request.getRefundAllowed());
        event.setPosterImage(request.getPosterImage());
        event.setOrganizerCommittee(request.getOrganizerCommittee());
        event.setCreatedAt(LocalDateTime.now());

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            event.setCategory(category);
        }

        // Set status based on submit flag
        if (request.isSubmitForApproval()) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        } else {
            event.setStatus(EventStatus.DRAFT);
        }

        event = eventRepository.save(event);

        // Create ticket types
        for (TicketTypeRequest ttRequest : request.getTicketTypes()) {
            TicketType ticketType = new TicketType(
                event,
                ttRequest.getTypeName(),
                ttRequest.getPrice(),
                ttRequest.getTicketQuantity()
            );
            ticketType.setDescription(ttRequest.getDescription());
            ticketType.setSaleStartDate(ttRequest.getSaleStartDate());
            ticketType.setSaleEndDate(ttRequest.getSaleEndDate());
            ticketTypeRepository.save(ticketType);
        }

        // Log action
        auditLogService.logEventCreation(organizer, event.getEventId(), event.getName());

        // Notify admin if submitted for approval
        if (request.isSubmitForApproval()) {
            // TODO: Notify admin of new event pending approval
            log.info("Event submitted for approval - Event ID: {}, Organizer: {}", 
                     event.getEventId(), organizer.getFullName());
        }

        log.info("Event created - Event ID: {}, Organizer ID: {}, Status: {}", 
                 event.getEventId(), organizerId, event.getStatus());

        return mapToEventResponse(event);
    }

    // ==================== EVENT RETRIEVAL ====================

    /**
     * Get organizer's events
     */
    public List<EventResponse> getOrganizerEvents(Long organizerId) {
        List<Event> events = eventRepository.findByOrganizer_UserIdOrderByCreatedAtDesc(organizerId);
        return events.stream()
            .map(this::mapToEventResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get event by ID (organizer view)
     */
    public EventResponse getEventById(Long organizerId, Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Verify organizer owns the event
        if (!event.getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("You do not have permission to view this event");
        }

        return mapToEventResponse(event);
    }

    /**
     * Get public event details (for customers)
     */
    public PublicEventResponse getPublicEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Only show ACTIVE events to public
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ResourceNotFoundException("Event not found or not available");
        }

        return mapToPublicEventResponse(event);
    }

    /**
     * Browse public events
     */
    public List<PublicEventResponse> browsePublicEvents() {
        List<Event> events = eventRepository.findByStatusOrderByStartDateAsc(EventStatus.ACTIVE);
        return events.stream()
            .map(this::mapToPublicEventResponse)
            .collect(Collectors.toList());
    }

    // ==================== EVENT UPDATE REQUEST (UC-03.2) ====================

    /**
     * UC-03.2: Request event update
     * FR3: Block price changes after sales started
     * FR15: Block updates to past events
     */
    @Transactional
    public EventUpdateRequestResponse requestEventUpdate(Long organizerId, Long eventId, UpdateEventRequest request) {
        // Get event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Verify organizer owns the event
        if (!event.getOrganizer().getUserId().equals(organizerId)) {
            throw new BusinessRuleViolationException("You do not have permission to update this event");
        }

        // FR15: Block updates to past events
        if (event.hasOccurred()) {
            throw new BusinessRuleViolationException("Cannot update event that has already occurred");
        }

        // Check if there's already a pending update request
        if (updateRequestRepository.existsByEvent_EventIdAndStatus(eventId, "PENDING_REVIEW")) {
            throw new BusinessRuleViolationException("There is already a pending update request for this event");
        }

        // Create update request
        EventUpdateRequest updateRequest = new EventUpdateRequest(event, event.getOrganizer(), request.getJustification());
        
        // Set proposed changes
        if (request.getName() != null) {
            updateRequest.setProposedName(request.getName());
        }
        if (request.getDescription() != null) {
            updateRequest.setProposedDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            updateRequest.setProposedStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            updateRequest.setProposedEndDate(request.getEndDate());
        }
        if (request.getLocation() != null) {
            updateRequest.setProposedLocation(request.getLocation());
        }
        if (request.getVenueName() != null) {
            updateRequest.setProposedVenueName(request.getVenueName());
        }
        if (request.getMaxTicketQuantity() != null) {
            updateRequest.setProposedMaxTicketQuantity(request.getMaxTicketQuantity());
        }
        if (request.getRefundAllowed() != null) {
            updateRequest.setProposedRefundAllowed(request.getRefundAllowed());
        }

        updateRequest = updateRequestRepository.save(updateRequest);

        // Log action
        auditLogService.logAction(organizerId, event.getOrganizer().getFullName(), 
                                  "REQUEST_EVENT_UPDATE", "EVENT_UPDATE_REQUEST", 
                                  updateRequest.getRequestId(), 
                                  "Update request submitted for event: " + event.getName());

        log.info("Event update requested - Event ID: {}, Request ID: {}", eventId, updateRequest.getRequestId());

        return mapToUpdateRequestResponse(updateRequest);
    }

    /**
     * Get organizer's update requests
     */
    public List<EventUpdateRequestResponse> getOrganizerUpdateRequests(Long organizerId) {
        List<EventUpdateRequest> requests = updateRequestRepository.findByOrganizer_UserIdOrderByRequestedAtDesc(organizerId);
        return requests.stream()
            .map(this::mapToUpdateRequestResponse)
            .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private EventResponse mapToEventResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setOrganizerId(event.getOrganizer().getUserId());
        response.setOrganizerName(event.getOrganizer().getFullName());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setLocation(event.getLocation());
        response.setVenueName(event.getVenueName());
        response.setMaxTicketQuantity(event.getMaxTicketQuantity());
        response.setStatus(event.getStatus());
        response.setRefundAllowed(event.getRefundAllowed());
        response.setPosterImage(event.getPosterImage());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }

    private PublicEventResponse mapToPublicEventResponse(Event event) {
        PublicEventResponse response = new PublicEventResponse();
        response.setEventId(event.getEventId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setEventType(event.getEventType());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());
        response.setLocation(event.getLocation());
        response.setVenueName(event.getVenueName());
        response.setPosterImage(event.getPosterImage());
        response.setOrganizerName(event.getOrganizer().getFullName());
        response.setStatus(event.getStatus());
        response.setRefundAllowed(event.getRefundAllowed());

        // Calculate total and available tickets
        List<TicketType> ticketTypes = ticketTypeRepository.findByEvent_EventId(event.getEventId());
        int totalTickets = ticketTypes.stream().mapToInt(TicketType::getTicketQuantity).sum();
        int availableTickets = ticketTypes.stream().mapToInt(TicketType::getAvailableQuantity).sum();
        
        response.setTotalTickets(totalTickets);
        response.setAvailableTickets(availableTickets);

        // Map ticket types
        List<PublicEventResponse.PublicTicketTypeInfo> publicTicketTypes = new ArrayList<>();
        for (TicketType tt : ticketTypes) {
            PublicEventResponse.PublicTicketTypeInfo ttInfo = new PublicEventResponse.PublicTicketTypeInfo();
            ttInfo.setTicketTypeId(tt.getTicketTypeId());
            ttInfo.setTypeName(tt.getTypeName());
            ttInfo.setDescription(tt.getDescription());
            ttInfo.setPrice(tt.getPrice());
            ttInfo.setAvailableQuantity(tt.getAvailableQuantity());
            publicTicketTypes.add(ttInfo);
        }
        response.setTicketTypes(publicTicketTypes);

        return response;
    }

    private EventUpdateRequestResponse mapToUpdateRequestResponse(EventUpdateRequest request) {
        EventUpdateRequestResponse response = new EventUpdateRequestResponse();
        response.setRequestId(request.getRequestId());
        response.setEventId(request.getEvent().getEventId());
        response.setEventName(request.getEvent().getName());
        response.setOrganizerId(request.getOrganizer().getUserId());
        response.setOrganizerName(request.getOrganizer().getFullName());
        response.setJustification(request.getJustification());
        response.setStatus(request.getStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());
        response.setAdminNotes(request.getAdminNotes());
        
        // Build summary of proposed changes
        StringBuilder changes = new StringBuilder();
        if (request.getProposedName() != null) changes.append("Name, ");
        if (request.getProposedDescription() != null) changes.append("Description, ");
        if (request.getProposedLocation() != null) changes.append("Location, ");
        if (request.getProposedStartDate() != null) changes.append("Start Date, ");
        if (request.getProposedEndDate() != null) changes.append("End Date, ");
        if (changes.length() > 0) {
            changes.setLength(changes.length() - 2); // Remove trailing comma
        }
        response.setProposedChanges(changes.toString());
        
        return response;
    }
}

