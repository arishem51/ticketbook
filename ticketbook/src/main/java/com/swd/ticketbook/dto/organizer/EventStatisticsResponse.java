package com.swd.ticketbook.dto.organizer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for event statistics (UC-03.3)
 * Business Rules: FR17, FR26
 */
public class EventStatisticsResponse {

    private Long eventId;
    private String eventName;
    
    // Sales Statistics
    private Integer totalTicketsSold;
    private Integer totalTicketsAvailable;
    private BigDecimal totalRevenue;
    private BigDecimal platformFee;
    private BigDecimal netRevenue;
    
    // Check-in Statistics
    private Integer totalCheckedIn;
    private Double checkInRate; // Percentage
    
    // Ticket Type Breakdown
    private List<TicketTypeSales> ticketTypeSales;
    
    // Time-based Statistics
    private Map<String, Integer> salesByDay; // Date -> Count
    private Map<String, BigDecimal> revenueByDay; // Date -> Revenue
    
    // Customer Demographics (anonymized)
    private Map<String, Integer> salesByRegion;
    
    // Refund Statistics
    private Integer totalRefunds;
    private BigDecimal totalRefundAmount;

    // Nested class for ticket type sales
    public static class TicketTypeSales {
        private String typeName;
        private BigDecimal price;
        private Integer totalQuantity;
        private Integer sold;
        private Integer available;
        private BigDecimal revenue;

        // Getters and Setters
        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public Integer getSold() {
            return sold;
        }

        public void setSold(Integer sold) {
            this.sold = sold;
        }

        public Integer getAvailable() {
            return available;
        }

        public void setAvailable(Integer available) {
            this.available = available;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(Integer totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public Integer getTotalTicketsAvailable() {
        return totalTicketsAvailable;
    }

    public void setTotalTicketsAvailable(Integer totalTicketsAvailable) {
        this.totalTicketsAvailable = totalTicketsAvailable;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }

    public Integer getTotalCheckedIn() {
        return totalCheckedIn;
    }

    public void setTotalCheckedIn(Integer totalCheckedIn) {
        this.totalCheckedIn = totalCheckedIn;
    }

    public Double getCheckInRate() {
        return checkInRate;
    }

    public void setCheckInRate(Double checkInRate) {
        this.checkInRate = checkInRate;
    }

    public List<TicketTypeSales> getTicketTypeSales() {
        return ticketTypeSales;
    }

    public void setTicketTypeSales(List<TicketTypeSales> ticketTypeSales) {
        this.ticketTypeSales = ticketTypeSales;
    }

    public Map<String, Integer> getSalesByDay() {
        return salesByDay;
    }

    public void setSalesByDay(Map<String, Integer> salesByDay) {
        this.salesByDay = salesByDay;
    }

    public Map<String, BigDecimal> getRevenueByDay() {
        return revenueByDay;
    }

    public void setRevenueByDay(Map<String, BigDecimal> revenueByDay) {
        this.revenueByDay = revenueByDay;
    }

    public Map<String, Integer> getSalesByRegion() {
        return salesByRegion;
    }

    public void setSalesByRegion(Map<String, Integer> salesByRegion) {
        this.salesByRegion = salesByRegion;
    }

    public Integer getTotalRefunds() {
        return totalRefunds;
    }

    public void setTotalRefunds(Integer totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(BigDecimal totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }
}

