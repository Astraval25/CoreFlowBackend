package com.astraval.coreflow.modules.vendor.facade;

import java.util.List;

import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.dto.UpdateVendorRequest;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

public interface VendorFacade {
    VendorProjection createVendor(Integer companyId, CreateVendorRequest request);
    List<VendorProjection> getAllVendors(Integer companyId);
    VendorProjection getVendorById(Integer companyId, Long vendorId);
    VendorProjection updateVendor(Integer companyId, Long vendorId, UpdateVendorRequest request);
    void deactivateVendor(Integer companyId, Long vendorId);
}