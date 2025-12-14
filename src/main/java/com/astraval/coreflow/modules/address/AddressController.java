package com.astraval.coreflow.modules.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import com.astraval.coreflow.modules.address.dto.UpdateAddressRequest;
import com.astraval.coreflow.modules.address.facade.AddressFacade;
import com.astraval.coreflow.modules.address.projection.AddressProjection;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/addresses")
public class AddressController {

  @Autowired
  private AddressFacade addressFacade;

  @GetMapping("/addressId/{addressId}")
  public ApiResponse<AddressProjection> getAddress(@PathVariable Integer addressId) {
    try {
      AddressProjection address = addressFacade.getAddressById(addressId);
      return ApiResponseFactory.accepted(address, "Address retrieved successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @PutMapping("/addressId/{addressId}")
  public ApiResponse<AddressProjection> updateAddress(@PathVariable Integer addressId, @Valid @RequestBody UpdateAddressRequest request) {
    try {
      AddressProjection address = addressFacade.updateAddress(addressId, request);
      return ApiResponseFactory.accepted(address, "Address updated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
  
  @DeleteMapping("/addressId/{addressId}")
  public ApiResponse<Void> deactivateAddress(@PathVariable Integer addressId) {
    try {
      addressFacade.deactivateAddress(addressId);
      return ApiResponseFactory.accepted(null, "Address deactivated successfully");
    } catch (Exception e) {
      return ApiResponseFactory.badRequest(e.getMessage());
    }
  }
}