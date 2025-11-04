package com.swd.ticketbook.controllers;

import com.swd.ticketbook.dto.ApiResponse;
import com.swd.ticketbook.dto.admin.*;
import com.swd.ticketbook.dto.auth.UserResponse;
import com.swd.ticketbook.dto.refund.RefundResponse;
import com.swd.ticketbook.entities.User;
import com.swd.ticketbook.enums.EventStatus;
import com.swd.ticketbook.enums.UserRole;
import com.swd.ticketbook.security.CurrentUser;
import com.swd.ticketbook.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Admin operations (UC-04)
 * Handles user management, event management, KYC approval, and refund approval
 * Business Rules: FR17, FR18, FR19, FR20, FR21, FR22, FR23
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ==================== USER MANAGEMENT (UC-04.1 - 04.5) ====================

    /**
     * UC-04.1: Get all users
     * GET /api/admin/users
     * 
     * @param role Optional role filter
     * @param isDeleted Optional deleted status filter
     * @param search Optional search term
     * @param admin Current authenticated admin
     * @return List of users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String search,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        List<UserResponse> users;
        
        if (role != null || isDeleted != null || search != null) {
            users = adminService.getUsersByFilters(role, isDeleted, search);
        } else {
            users = adminService.getAllUsers();
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(users, "Users retrieved successfully")
        );
    }

    /**
     * UC-04.2: Get user by ID
     * GET /api/admin/users/{userId}
     * 
     * @param userId User ID to retrieve
     * @param admin Current authenticated admin
     * @return User details
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long userId,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        UserResponse user = adminService.getUserById(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success(user, "User details retrieved successfully")
        );
    }

    /**
     * UC-04.3: Update user
     * PUT /api/admin/users/{userId}
     * 
     * @param userId User ID to update
     * @param request Update request
     * @param admin Current authenticated admin
     * @return Updated user
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        UserResponse updatedUser = adminService.updateUser(admin.getUserId(), userId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedUser, "User updated successfully")
        );
    }

    /**
     * UC-04.4: Change user status (activate/deactivate)
     * PATCH /api/admin/users/{userId}/status
     * 
     * @param userId User ID
     * @param isDeleted New deleted status
     * @param admin Current authenticated admin
     * @return Updated user
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean isDeleted,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        UserResponse updatedUser = adminService.changeUserStatus(admin.getUserId(), userId, isDeleted);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedUser, "User status updated successfully")
        );
    }

    /**
     * UC-04.5: Delete user (soft delete)
     * DELETE /api/admin/users/{userId}
     * 
     * @param userId User ID to delete
     * @param admin Current authenticated admin
     * @return Success message
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        adminService.deleteUser(admin.getUserId(), userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("User deleted successfully")
        );
    }

    // ==================== EVENT MANAGEMENT (UC-04.6 - 04.10) ====================

    /**
     * UC-04.6: Get all events
     * GET /api/admin/events
     * 
     * @param status Optional status filter
     * @param admin Current authenticated admin
     * @return List of events
     */
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @RequestParam(required = false) EventStatus status,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        List<EventResponse> events;
        
        if (status != null) {
            events = adminService.getEventsByStatus(status);
        } else {
            events = adminService.getAllEvents();
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(events, "Events retrieved successfully")
        );
    }

    /**
     * UC-04.7: Get event by ID
     * GET /api/admin/events/{eventId}
     * 
     * @param eventId Event ID to retrieve
     * @param admin Current authenticated admin
     * @return Event details
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @PathVariable Long eventId,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        EventResponse event = adminService.getEventById(eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success(event, "Event details retrieved successfully")
        );
    }

    /**
     * UC-04.8: Update event
     * PUT /api/admin/events/{eventId}
     * 
     * @param eventId Event ID to update
     * @param request Update request
     * @param admin Current authenticated admin
     * @return Updated event
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody AdminEventUpdateRequest request,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        EventResponse updatedEvent = adminService.updateEvent(admin.getUserId(), eventId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedEvent, "Event updated successfully")
        );
    }

    /**
     * UC-04.9: Approve or reject event
     * POST /api/admin/events/{eventId}/approval
     * 
     * @param eventId Event ID to approve/reject
     * @param request Approval decision with notes
     * @param admin Current authenticated admin
     * @return Updated event
     */
    @PostMapping("/events/{eventId}/approval")
    public ResponseEntity<ApiResponse<EventResponse>> approveEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventApprovalRequest request,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        EventResponse event = adminService.approveEvent(admin.getUserId(), eventId, request);
        
        String message = request.isApproved() ? 
            "Event approved successfully" : "Event rejected successfully";
        
        return ResponseEntity.ok(
            ApiResponse.success(event, message)
        );
    }

    /**
     * UC-04.9: Change event status
     * PATCH /api/admin/events/{eventId}/status
     * 
     * @param eventId Event ID
     * @param status New status
     * @param admin Current authenticated admin
     * @return Updated event
     */
    @PatchMapping("/events/{eventId}/status")
    public ResponseEntity<ApiResponse<EventResponse>> changeEventStatus(
            @PathVariable Long eventId,
            @RequestParam EventStatus status,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        EventResponse updatedEvent = adminService.changeEventStatus(admin.getUserId(), eventId, status);
        
        return ResponseEntity.ok(
            ApiResponse.success(updatedEvent, "Event status updated successfully")
        );
    }

    /**
     * UC-04.10: Delete event (soft delete)
     * DELETE /api/admin/events/{eventId}
     * 
     * @param eventId Event ID to delete
     * @param admin Current authenticated admin
     * @return Success message
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long eventId,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        adminService.deleteEvent(admin.getUserId(), eventId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Event deleted successfully")
        );
    }

    // ==================== KYC VERIFICATION ====================

    /**
     * Get pending KYC verifications
     * GET /api/admin/kyc/pending
     * 
     * @param admin Current authenticated admin
     * @return List of pending KYC verifications
     */
    @GetMapping("/kyc/pending")
    public ResponseEntity<ApiResponse<List<OrganizerProfileResponse>>> getPendingKycVerifications(
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        List<OrganizerProfileResponse> profiles = adminService.getPendingKycVerifications();
        
        return ResponseEntity.ok(
            ApiResponse.success(profiles, "Pending KYC verifications retrieved successfully")
        );
    }

    /**
     * Get KYC verification details
     * GET /api/admin/kyc/{organizerId}
     * 
     * @param organizerId Organizer ID
     * @param admin Current authenticated admin
     * @return KYC verification details
     */
    @GetMapping("/kyc/{organizerId}")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> getKycVerification(
            @PathVariable Long organizerId,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        OrganizerProfileResponse profile = adminService.getKycVerificationById(organizerId);
        
        return ResponseEntity.ok(
            ApiResponse.success(profile, "KYC verification details retrieved successfully")
        );
    }

    /**
     * Approve or reject KYC verification
     * POST /api/admin/kyc/{organizerId}/approval
     * 
     * @param organizerId Organizer ID
     * @param request Approval decision with notes
     * @param admin Current authenticated admin
     * @return Updated organizer profile
     */
    @PostMapping("/kyc/{organizerId}/approval")
    public ResponseEntity<ApiResponse<OrganizerProfileResponse>> processKycVerification(
            @PathVariable Long organizerId,
            @Valid @RequestBody KycApprovalRequest request,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        OrganizerProfileResponse profile = adminService.processKycVerification(
            admin.getUserId(), organizerId, request
        );
        
        String message = request.isApproved() ? 
            "KYC verification approved successfully. User upgraded to Verified Organizer." : 
            "KYC verification rejected successfully";
        
        return ResponseEntity.ok(
            ApiResponse.success(profile, message)
        );
    }

    // ==================== REFUND APPROVAL ====================

    /**
     * Get pending refund requests
     * GET /api/admin/refunds/pending
     * 
     * @param admin Current authenticated admin
     * @return List of pending refunds
     */
    @GetMapping("/refunds/pending")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPendingRefunds(
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        List<RefundResponse> refunds = adminService.getPendingRefunds();
        
        return ResponseEntity.ok(
            ApiResponse.success(refunds, "Pending refunds retrieved successfully")
        );
    }

    /**
     * Approve or reject refund request
     * POST /api/admin/refunds/{refundId}/approval
     * 
     * @param refundId Refund request ID
     * @param request Approval decision with amount and notes
     * @param admin Current authenticated admin
     * @return Updated refund
     */
    @PostMapping("/refunds/{refundId}/approval")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody RefundApprovalRequest request,
            @CurrentUser User admin) {
        
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied. Admin privileges required."));
        }
        
        RefundResponse refund = adminService.processRefund(admin.getUserId(), refundId, request);
        
        String message = request.getApproved() ? 
            "Refund approved and processed successfully" : 
            "Refund request rejected successfully";
        
        return ResponseEntity.ok(
            ApiResponse.success(refund, message)
        );
    }
}

