package com.astraval.coreflow.modules.customer.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.config.validation.ValidPAN;
import com.astraval.coreflow.modules.address.dto.CreateUpdateAddressDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpdateCustomerDto {

  @NotBlank(message = "Customer name is required")
  private String customerName;
  
  @NotBlank(message = "Display name is required")
  private String displayName;

  @Email(message = "Invalid email format")
  private String email;

  private String phone;

  private String lang;

  @ValidPAN(message = "Invalid PAN format")
  private String pan;

  private String gst;

  private BigDecimal advanceAmount;
  
  private boolean sameAsBillingAddress;
  
  private CreateUpdateAddressDto shippingAddress;
  
  private CreateUpdateAddressDto billingAddress;
}
