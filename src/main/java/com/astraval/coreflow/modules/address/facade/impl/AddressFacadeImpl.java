package com.astraval.coreflow.modules.address.facade.impl;

import com.astraval.coreflow.modules.address.facade.AddressFacade;
import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressFacadeImpl implements AddressFacade {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Address getAddressById(Integer addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }

    @Override
    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }
}