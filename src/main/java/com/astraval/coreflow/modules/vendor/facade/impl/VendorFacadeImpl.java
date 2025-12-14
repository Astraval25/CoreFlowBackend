package com.astraval.coreflow.modules.vendor.facade.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.vendor.VendorService;
import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.facade.VendorFacade;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

@Service
public class VendorFacadeImpl implements VendorFacade {

    @Autowired
    private VendorService vendorService;

    @Override
    public VendorProjection createVendor(CreateVendorRequest request) {
        return vendorService.createVendor(request);
    }

    @Override
    public List<VendorProjection> getAllVendors() {
        return vendorService.getAllVendors();
    }
}