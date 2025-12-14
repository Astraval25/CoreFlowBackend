package com.astraval.coreflow.modules.vendor.facade;

import java.util.List;

import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

public interface VendorFacade {
    VendorProjection createVendor(CreateVendorRequest request);
    List<VendorProjection> getAllVendors();
}