package com.astraval.coreflow.modules.address.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressService;
import com.astraval.coreflow.modules.address.dto.UpdateAddressRequest;
import com.astraval.coreflow.modules.address.facade.AddressFacade;
import com.astraval.coreflow.modules.address.projection.AddressProjection;

@Service
public class AddressFacadeImpl implements AddressFacade {

    @Autowired
    private AddressService addressService;

    @Override
    public Address createAddress(Address address) {
        // Authorization handled by calling service (CustomerService/VendorService)
        return addressService.createAddress(address);
    }

    @Override
    public AddressProjection getAddressById(Integer addressId) {
        return addressService.getAddressById(addressId);
    }

    @Override
    public AddressProjection updateAddress(Integer addressId, UpdateAddressRequest request) {
        return addressService.updateAddress(addressId, request);
    }

    @Override
    public void deactivateAddress(Integer addressId) {
        addressService.deactivateAddress(addressId);
    }
}