package com.swd.ticketbook.dto.admin;

import com.swd.ticketbook.enums.EventStatus;

import java.time.LocalDateTime;

/**
 * DTO for Event response (Admin view)
 */
public class EventResponse {

    private Long eventId;
    private Long organizerId;
    private String organizerName;
    private String name;
    private String description;
    private String eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String venueName;
    private Integer maxTicketQuantity;
    private EventStatus status;
    private Boolean refundAllowed;
    private String posterImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public EventResponse() {
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

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

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

