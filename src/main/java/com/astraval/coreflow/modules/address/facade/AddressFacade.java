package com.astraval.coreflow.modules.address.facade;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.dto.UpdateAddressRequest;
import com.astraval.coreflow.modules.address.projection.AddressProjection;

public interface AddressFacade {
    Address createAddress(Address address);
    AddressProjection getAddressById(Integer addressId);
    AddressProjection updateAddress(Integer addressId, UpdateAddressRequest request);
    void deactivateAddress(Integer addressId);
}