package com.astraval.coreflow.modules.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import java.util.List;

import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.dto.UpdateVendorRequest;
import com.astraval.coreflow.modules.vendor.facade.VendorFacade;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/companies/{companyId}/vendors")
public class VendorController {

  @Autowired
  private VendorFacade vendorFacade;

  @PostMapping
  public ApiResponse<VendorProjection> createVendor(@PathVariable Integer companyId, @Valid @RequestBody CreateVendorRequest request) {
    try {
      VendorProjection vendor = vendorFacade.createVendor(companyId, request);
      return ApiResponseFactory.accepted(vendor, "Vendor created successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @GetMapping
  public ApiResponse<List<VendorProjection>> getAllVendors(@PathVariable Integer companyId) {
    try {
      List<VendorProjection> vendors = vendorFacade.getAllVendors(companyId);
      return ApiResponseFactory.accepted(vendors, "Vendors retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @GetMapping("/{vendorId}")
  public ApiResponse<VendorProjection> getVendorById(@PathVariable Integer companyId, @PathVariable Long vendorId) {
    try {
      VendorProjection vendor = vendorFacade.getVendorById(companyId, vendorId);
      return ApiResponseFactory.accepted(vendor, "Vendor retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @PutMapping("/{vendorId}")
  public ApiResponse<VendorProjection> updateVendor(@PathVariable Integer companyId, @PathVariable Long vendorId, @Valid @RequestBody UpdateVendorRequest request) {
    try {
      VendorProjection vendor = vendorFacade.updateVendor(companyId, vendorId, request);
      return ApiResponseFactory.accepted(vendor, "Vendor updated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @DeleteMapping("/{vendorId}")
  public ApiResponse<Void> deactivateVendor(@PathVariable Integer companyId, @PathVariable Long vendorId) {
    try {
      vendorFacade.deactivateVendor(companyId, vendorId);
      return ApiResponseFactory.accepted(null, "Vendor deactivated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
}