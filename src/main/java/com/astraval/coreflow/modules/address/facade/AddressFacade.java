package com.astraval.coreflow.modules.address.facade;

import com.astraval.coreflow.modules.address.Address;

public interface AddressFacade {
    Address getAddressById(Integer addressId);
    Address createAddress(Address address);
}