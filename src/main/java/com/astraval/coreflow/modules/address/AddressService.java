package com.astraval.coreflow.modules.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.modules.address.dto.CreateUpdateAddressDto;
import com.astraval.coreflow.modules.address.projection.AddressProjection;


@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private SecurityUtil securityUtil;
    
    @Transactional
    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }
    @ReadOnlyProperty 
    public AddressProjection getAddressById(Integer addressId) {
        String currentUserId = securityUtil.getCurrentSub();
        
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found"));
            
        if (!address.getIsActive()) {
            throw new RuntimeException("Address is not active");
        }
        
        if (!currentUserId.equals(address.getCreatedBy())) {
            throw new RuntimeException("Access denied: Address does not belong to current user");
        }
        
        return addressMapper.toProjection(address);
    }
    
    @Transactional
    public AddressProjection updateAddress(Integer addressId, CreateUpdateAddressDto request) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found"));
            
        if (!address.getIsActive()) {
            throw new RuntimeException("Address is not active");
        }
        
        if (!userIdStr.equals(address.getCreatedBy())) {
            throw new RuntimeException("Access denied: Address does not belong to current user");
        }
        
        // Update address fields
        address.setAttentionName(request.getAttentionName());
        address.setCountry(request.getCountry());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setPhone(request.getPhone());
        address.setEmail(request.getEmail());
        
        address = addressRepository.save(address);
        return addressMapper.toProjection(address);
    }
    
    @Transactional
    public void updateAddress(Integer addressId, Address updatedAddress) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found"));
            
        address.setAttentionName(updatedAddress.getAttentionName());
        address.setCountry(updatedAddress.getCountry());
        address.setLine1(updatedAddress.getLine1());
        address.setLine2(updatedAddress.getLine2());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setPincode(updatedAddress.getPincode());
        address.setPhone(updatedAddress.getPhone());
        address.setEmail(updatedAddress.getEmail());
        
        addressRepository.save(address);
    }
    
    @Transactional
    public void deactivateAddress(Integer addressId) {
        String userIdStr = securityUtil.getCurrentSub();
        
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found"));
            
        if (!userIdStr.equals(address.getCreatedBy())) {
            throw new RuntimeException("Access denied: Address does not belong to current user");
        }
        
        address.setIsActive(false);
        
        addressRepository.save(address);
    }
}