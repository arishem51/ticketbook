package com.swd.ticketbook.dto.organizer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for creating a new event (UC-03.1)
 * Business Rules: FR2, FR15, FR17, FR26
 */
public class CreateEventRequest {

    @NotBlank(message = "Event name is required")
    @Size(max = 500, message = "Event name must not exceed 500 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String eventType;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @Size(max = 255, message = "Venue name must not exceed 255 characters")
    private String venueName;

    @Min(value = 1, message = "Max ticket quantity must be at least 1")
    private Integer maxTicketQuantity;

    @NotNull(message = "Refund policy is required")
    private Boolean refundAllowed;

    @Size(max = 500, message = "Poster image URL must not exceed 500 characters")
    private String posterImage;

    @Size(max = 1000, message = "Organizer committee must not exceed 1000 characters")
    private String organizerCommittee;

    private Long categoryId;

    @NotNull(message = "At least one ticket type is required")
    @Size(min = 1, message = "At least one ticket type is required")
    @Valid
    private List<TicketTypeRequest> ticketTypes;

    private boolean submitForApproval = false; // true = submit, false = save as draft

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Integer getMaxTicketQuantity() {
        return maxTicketQuantity;
    }

    public void setMaxTicketQuantity(Integer maxTicketQuantity) {
        this.maxTicketQuantity = maxTicketQuantity;
    }

    public Boolean getRefundAllowed() {
        return refundAllowed;
    }

    public void setRefundAllowed(Boolean refundAllowed) {
        this.refundAllowed = refundAllowed;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getOrganizerCommittee() {
        return organizerCommittee;
    }

    public void setOrganizerCommittee(String organizerCommittee) {
        this.organizerCommittee = organizerCommittee;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<TicketTypeRequest> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<TicketTypeRequest> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    public boolean isSubmitForApproval() {
        return submitForApproval;
    }

    public void setSubmitForApproval(boolean submitForApproval) {
        this.submitForApproval = submitForApproval;
    }
}

