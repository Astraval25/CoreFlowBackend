package com.astraval.coreflow.modules.employees.mapper;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.address.dto.CreateUpdateAddressDto;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.employees.Employees;
import com.astraval.coreflow.modules.employees.dto.EmployeeCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EmployeeMapper {

    /**
     * Maps the DTO and the fetched Company into a single Employee Entity.
     * Note: 'personalemail' (DTO) is mapped to 'personalEmail' (Entity).
     */
    @Mapping(source = "dto.personalemail", target = "personalEmail")
    @Mapping(source = "company", target = "company")
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Employees toEntity(EmployeeCreateDto dto, Companies company);

    /**
     * MapStruct automatically calls this method when it sees the 
     * 'address' field inside EmployeeCreateDto.
     */
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Address toAddressEntity(CreateUpdateAddressDto dto);
}