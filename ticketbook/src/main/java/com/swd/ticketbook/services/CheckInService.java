package com.swd.ticketbook.services;

import com.swd.ticketbook.dto.checkin.CheckInRequest;
import com.swd.ticketbook.dto.checkin.CheckInResponse;
import com.swd.ticketbook.entities.Event;
import com.swd.ticketbook.entities.Ticket;
import com.swd.ticketbook.enums.TicketStatus;
import com.swd.ticketbook.exceptions.ResourceNotFoundException;
import com.swd.ticketbook.repositories.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for Check-in operations (UC-02.3)
 * Business Rules: FR6, FR8, FR20
 */
@Service
public class CheckInService {

    private static final Logger log = LoggerFactory.getLogger(CheckInService.class);

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * UC-02.3: Check-in with QR Code
     * FR6: Validate unique QR code and single-use
     * FR8: Reject refunded/cancelled tickets
     * FR20: Log all check-in attempts
     */
    @Transactional
    public CheckInResponse checkIn(CheckInRequest request) {
        CheckInResponse response = new CheckInResponse();
        
        try {
            // FR6: Validate QR code format and find ticket
            Ticket ticket = ticketRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid QR code - Ticket not recognized"));

            // Verify ticket status is CONFIRMED
            if (ticket.getStatus() != TicketStatus.CONFIRMED) {
                String reason = getStatusRejectionReason(ticket);
                response.setSuccess(false);
                response.setMessage(reason);
                
                // FR20: Log failed check-in
                logCheckInAttempt(ticket, false, reason);
                return response;
            }

            // FR6: Verify ticket hasn't been used (single-use)
            if (ticket.getCheckInDateTime() != null) {
                String usedTime = ticket.getCheckInDateTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                String message = "✗ Already Used - Checked in at " + usedTime;
                response.setSuccess(false);
                response.setMessage(message);
                
                // FR20: Log duplicate check-in attempt
                logCheckInAttempt(ticket, false, "Duplicate entry attempt");
                return response;
            }

            // Verify current date/time is within event period
            Event event = ticket.getOrder().getEvent();
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isBefore(event.getStartDate())) {
                String message = "✗ Too Early - Event starts on " + 
                    event.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
                response.setSuccess(false);
                response.setMessage(message);
                
                // FR20: Log early check-in attempt
                logCheckInAttempt(ticket, false, "Event not started");
                return response;
            }

            if (now.isAfter(event.getEndDate())) {
                String message = "✗ Event Ended on " + 
                    event.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                response.setSuccess(false);
                response.setMessage(message);
                
                // FR20: Log late check-in attempt
                logCheckInAttempt(ticket, false, "Event already ended");
                return response;
            }

            // All validations passed - Check in the ticket
            ticket.checkIn();
            ticketRepository.save(ticket);

            // Build success response
            response.setSuccess(true);
            response.setMessage("✓ Valid - Entry Granted");
            response.setTicketId(ticket.getTicketId());
            response.setCustomerName(ticket.getOrder().getUser().getFullName());
            response.setEventName(event.getName());
            response.setTicketTypeName(ticket.getTicketType().getTypeName());
            response.setSeatNumber(ticket.getSeatNumber());
            response.setCheckInDateTime(ticket.getCheckInDateTime());

            // FR20: Log successful check-in
            logCheckInAttempt(ticket, true, "Entry granted");

            return response;

        } catch (ResourceNotFoundException e) {
            response.setSuccess(false);
            response.setMessage("✗ Invalid Ticket - Not Recognized");
            
            // FR20: Log invalid QR code attempt
            log.warn("Check-in failed: Invalid QR code - {}", request.getQrCode());
            
            return response;
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("✗ System Error - Please contact staff");
            
            // FR20: Log system error
            log.error("Check-in system error for QR: {}", request.getQrCode(), e);
            
            return response;
        }
    }

    /**
     * Get rejection reason based on ticket status (FR8)
     */
    private String getStatusRejectionReason(Ticket ticket) {
        return switch (ticket.getStatus()) {
            case REFUNDED -> "✗ Ticket Invalid - Refunded";
            case CANCELLED -> "✗ Ticket Invalid - Cancelled";
            case USED -> "✗ Already Used - Checked in at " + 
                ticket.getCheckInDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            default -> "✗ Ticket Not Valid - Payment Incomplete";
        };
    }

    /**
     * FR20: Log check-in attempt for audit trail
     */
    private void logCheckInAttempt(Ticket ticket, boolean success, String reason) {
        String logMessage = String.format(
            "Check-in %s - Ticket: %d, QR: %s, Customer: %s, Event: %s, Reason: %s, Time: %s",
            success ? "SUCCESS" : "FAILED",
            ticket.getTicketId(),
            ticket.getQrCode(),
            ticket.getOrder().getUser().getFullName(),
            ticket.getOrder().getEvent().getName(),
            reason,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        if (success) {
            log.info(logMessage);
        } else {
            log.warn(logMessage);
        }
    }

    /**
     * Get ticket details by QR code (for scanner preview)
     */
    public CheckInResponse getTicketDetails(String qrCode) {
        CheckInResponse response = new CheckInResponse();
        
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        response.setTicketId(ticket.getTicketId());
        response.setCustomerName(ticket.getOrder().getUser().getFullName());
        response.setEventName(ticket.getOrder().getEvent().getName());
        response.setTicketTypeName(ticket.getTicketType().getTypeName());
        response.setSeatNumber(ticket.getSeatNumber());
        response.setCheckInDateTime(ticket.getCheckInDateTime());
        response.setSuccess(ticket.canCheckIn());
        response.setMessage(ticket.canCheckIn() ? "Ready for check-in" : "Already used or invalid");

        return response;
    }
}

