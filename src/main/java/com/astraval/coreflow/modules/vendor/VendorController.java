package com.astraval.coreflow.modules.vendor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.vendor.dto.CreateUpdateVendorDto;
import com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    // Create
    @PostMapping("/{companyId}/vendors")
    public ApiResponse<Map<String, Long>> createVendor(@PathVariable Long companyId,
            @Valid @RequestBody CreateUpdateVendorDto request) {
        try {
            Long vendorId = vendorService.createVendor(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("vendorId", vendorId),
                    "Vendor created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    // Read
    @GetMapping("/vendors") // get all vendors (without filter)
    public ApiResponse<List<Vendors>> getAllVendors() {
        try {
            List<Vendors> vendors = vendorService.getAllVendors();
            return ApiResponseFactory.accepted(vendors, "vendors retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/vendors") // get all vendors (by companyId)
    public ApiResponse<List<VendorSummaryDto>> getVendorsByCompany(@PathVariable Long companyId) {
        try {
            List<VendorSummaryDto> vendors = vendorService.getVendorsByCompany(companyId);
            return ApiResponseFactory.accepted(vendors, "vendors retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/vendors/active") // get all vendors (by companyId & is_active - true)
    public ApiResponse<List<VendorSummaryDto>> getActiveVendorsByCompany(@PathVariable Long companyId) {
        try {
            List<VendorSummaryDto> vendors = vendorService.getActiveVendorsByCompany(companyId);
            return ApiResponseFactory.accepted(vendors, "vendors retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @GetMapping("/{companyId}/vendors/unlinked") // get all vendors (by companyId & vendorCompany is null)
    public ApiResponse<List<VendorSummaryDto>> getUnlinkedVendorsByCompany(@PathVariable Long companyId) {
        try {
            List<VendorSummaryDto> vendors = vendorService.getUnlinkedVendorsByCompany(companyId);
            return ApiResponseFactory.accepted(vendors, "vendors retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/vendors/{id}") // get vendor detail (check company id and vendor id both)
    public ApiResponse<Vendors> getVendorById(@PathVariable Long companyId, @PathVariable Long id) {
        try {
            Vendors vendor = vendorService.getVendorById(companyId, id);
            return ApiResponseFactory.accepted(vendor, "vendor retrieved successful");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Update
    @PutMapping("/{companyId}/vendors/{id}")
    public ApiResponse<Vendors> updateVendor(@PathVariable Long companyId, @PathVariable Long id,
            @Valid @RequestBody CreateUpdateVendorDto request) {
        try {
            vendorService.updateVendor(companyId, id, request);
            return ApiResponseFactory.updated(null, "Vendor updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/{companyId}/vendors/{id}/deactivate")
    public ApiResponse<String> deactivateVendor(@PathVariable Long id) {
        try {
            vendorService.deactivateVendor(id);
            return ApiResponseFactory.updated(null, "Vendor deactivated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    @PatchMapping("/{companyId}/vendors/{id}/activate")
    public ApiResponse<String> activateVendor(@PathVariable Long id) {
        try {
            vendorService.activateVendor(id);
            return ApiResponseFactory.updated(null, "Vendor activated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }

    // Delete
    @DeleteMapping("/{companyId}/vendors/{id}")
    public ApiResponse<String> deleteVendor(@PathVariable Long id) {
        try {
            vendorService.deleteVendor(id);
            return ApiResponseFactory.deleted("Vendor deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 420);
        }
    }
}
