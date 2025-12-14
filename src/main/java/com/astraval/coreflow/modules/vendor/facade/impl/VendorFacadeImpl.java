package com.astraval.coreflow.modules.vendor.facade.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.vendor.VendorService;
import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.dto.UpdateVendorRequest;
import com.astraval.coreflow.modules.vendor.facade.VendorFacade;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

@Service
public class VendorFacadeImpl implements VendorFacade {

    @Autowired
    private VendorService vendorService;

    @Override
    public VendorProjection createVendor(Integer companyId, CreateVendorRequest request) {
        return vendorService.createVendor(companyId, request);
    }

    @Override
    public List<VendorProjection> getAllVendors(Integer companyId) {
        return vendorService.getAllVendors(companyId);
    }

    @Override
    public VendorProjection getVendorById(Integer companyId, Long vendorId) {
        return vendorService.getVendorById(companyId, vendorId);
    }

    @Override
    public VendorProjection updateVendor(Integer companyId, Long vendorId, UpdateVendorRequest request) {
        return vendorService.updateVendor(companyId, vendorId, request);
    }

    @Override
    public void deactivateVendor(Integer companyId, Long vendorId) {
        vendorService.deactivateVendor(companyId, vendorId);
    }
}