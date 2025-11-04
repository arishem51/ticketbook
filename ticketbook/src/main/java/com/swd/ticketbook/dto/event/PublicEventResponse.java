package com.swd.ticketbook.dto.event;

import com.swd.ticketbook.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for public event response (browsing)
 */
public class PublicEventResponse {

    private Long eventId;
    private String name;
    private String description;
    private String eventType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String venueName;
    private String posterImage;
    private String organizerName;
    private EventStatus status;
    private Boolean refundAllowed;
    private Integer totalTickets;
    private Integer availableTickets;
    private List<PublicTicketTypeInfo> ticketTypes;

    public static class PublicTicketTypeInfo {
        private Long ticketTypeId;
        private String typeName;
        private String description;
        private java.math.BigDecimal price;
        private Integer availableQuantity;

        // Getters and Setters
        public Long getTicketTypeId() {
            return ticketTypeId;
        }

        public void setTicketTypeId(Long ticketTypeId) {
            this.ticketTypeId = ticketTypeId;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public java.math.BigDecimal getPrice() {
            return price;
        }

        public void setPrice(java.math.BigDecimal price) {
            this.price = price;
        }

        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        public void setAvailableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
        }
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
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

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
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

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Integer getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(Integer availableTickets) {
        this.availableTickets = availableTickets;
    }

    public List<PublicTicketTypeInfo> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<PublicTicketTypeInfo> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }
}

