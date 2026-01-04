package com.astraval.coreflow.modules.orderauditlog;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.orderauditlog.dto.OrderAuditLogDto;

@Mapper(componentModel = "spring")
public interface OrderAuditLogMapper {
    
    OrderAuditLogMapper INSTANCE = Mappers.getMapper(OrderAuditLogMapper.class);
    
    OrderAuditLogDto toOrderAuditLogDto(OrderAuditLog orderAuditLog);
    
    OrderAuditLog toOrderAuditLog(OrderAuditLogDto orderAuditLogDto);
}
