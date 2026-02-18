package com.astraval.coreflow.modules.invitation;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.invitation.dto.AcceptInvitationDto;
import com.astraval.coreflow.modules.invitation.dto.CreateInvitationDto;
import com.astraval.coreflow.modules.invitation.dto.InvitationViewDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/{companyId}/invitations/customers/{customerId}")
    public ApiResponse<Map<String, String>> sendCustomerInvite(
            @PathVariable Long companyId,
            @PathVariable Long customerId,
            @Valid @RequestBody(required = false) CreateInvitationDto request) {
        try {
            String email = request != null ? request.getEmail() : null;
            Invitation invitation = invitationService.sendCustomerInvite(companyId, customerId, email);
            return ApiResponseFactory.created(
                    Map.of(
                            "inviteId", invitation.getInviteId().toString(),
                            "invitationCode", invitation.getInvitationCode()),
                    "Invitation sent successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping("/{companyId}/invitations/vendors/{vendorId}")
    public ApiResponse<Map<String, String>> sendVendorInvite(
            @PathVariable Long companyId,
            @PathVariable Long vendorId,
            @Valid @RequestBody(required = false) CreateInvitationDto request) {
        try {
            String email = request != null ? request.getEmail() : null;
            Invitation invitation = invitationService.sendVendorInvite(companyId, vendorId, email);
            return ApiResponseFactory.created(
                    Map.of(
                            "inviteId", invitation.getInviteId().toString(),
                            "invitationCode", invitation.getInvitationCode()),
                    "Invitation sent successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/invitations/{invitationCode}")
    public ApiResponse<InvitationViewDto> getInvitation(
            @PathVariable Long companyId,
            @PathVariable String invitationCode) {
        try {
            InvitationViewDto invitation = invitationService.getInvitationByCode(invitationCode);
            return ApiResponseFactory.accepted(invitation, "Invitation retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/invitations/customers/{customerId}/code")
    public ApiResponse<Map<String, String>> getCustomerInvitationCode(
            @PathVariable Long companyId,
            @PathVariable Long customerId) {
        try {
            Invitation invitation = invitationService.getLatestCustomerInvitationByCreator(companyId, customerId);
            return ApiResponseFactory.accepted(
                    Map.of(
                            "inviteId", invitation.getInviteId().toString(),
                            "invitationCode", invitation.getInvitationCode()),
                    "Invitation code retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/invitations/vendors/{vendorId}/code")
    public ApiResponse<Map<String, String>> getVendorInvitationCode(
            @PathVariable Long companyId,
            @PathVariable Long vendorId) {
        try {
            Invitation invitation = invitationService.getLatestVendorInvitationByCreator(companyId, vendorId);
            return ApiResponseFactory.accepted(
                    Map.of(
                            "inviteId", invitation.getInviteId().toString(),
                            "invitationCode", invitation.getInvitationCode()),
                    "Invitation code retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping("/{companyId}/invitations/{invitationCode}/accept")
    public ApiResponse<String> acceptInvitation(
            @PathVariable Long companyId,
            @PathVariable String invitationCode,
            @Valid @RequestBody AcceptInvitationDto request) {
        try {
            invitationService.acceptInvitation(companyId, invitationCode, request);
            return ApiResponseFactory.accepted("Invitation accepted successfully", "Invitation accepted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping("/{companyId}/invitations/{invitationCode}/reject")
    public ApiResponse<String> rejectInvitation(
            @PathVariable Long companyId,
            @PathVariable String invitationCode) {
        try {
            invitationService.rejectInvitation(companyId, invitationCode);
            return ApiResponseFactory.accepted("Invitation rejected successfully", "Invitation rejected successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
