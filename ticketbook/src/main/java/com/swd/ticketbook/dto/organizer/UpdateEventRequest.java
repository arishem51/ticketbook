package com.swd.ticketbook.dto.organizer;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO for requesting event update (UC-03.2)
 * Business Rules: FR3, FR15, FR17, FR26
 */
public class UpdateEventRequest {

    @NotBlank(message = "Justification is required")
    @Size(min = 10, message = "Justification must be at least 10 characters")
    private String justification;

    @Size(max = 500, message = "Event name must not exceed 500 characters")
    private String name;

    private String description;

    @Size(max = 100, message = "Event type must not exceed 100 characters")
    private String eventType;

    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @Size(max = 255, message = "Venue name must not exceed 255 characters")
    private String venueName;

    private Integer maxTicketQuantity;

    private Boolean refundAllowed;

    @Size(max = 500, message = "Poster image URL must not exceed 500 characters")
    private String posterImage;

    // Getters and Setters
    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
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
}

