package com.astraval.coreflow.modules.vendor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import java.util.List;

import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.facade.VendorFacade;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/vendors")
public class VendorController {

  @Autowired
  private VendorFacade vendorFacade;

  @PostMapping
  public ApiResponse<VendorProjection> createVendor(@Valid @RequestBody CreateVendorRequest request) {
    try {
      VendorProjection vendor = vendorFacade.createVendor(request);
      return ApiResponseFactory.accepted(vendor, "Vendor created successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @GetMapping
  public ApiResponse<List<VendorProjection>> getAllVendors() {
    try {
      List<VendorProjection> vendors = vendorFacade.getAllVendors();
      return ApiResponseFactory.accepted(vendors, "Vendors retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
}