package com.astraval.coreflow.modules.vendor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto;

@Repository
public interface VendorRepository extends JpaRepository<Vendors, Long> {
    
    @Query("SELECT new com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto(" +
           "v.vendorId, " +
           "v.displayName, " +
           "COALESCE(vc.companyName, ''), " +
           "v.email, " + 
           "v.dueAmount, " +
           "v.isActive) " +
           "FROM Vendors v " +
           "LEFT JOIN v.vendorCompany vc " +
                  "WHERE v.company.companyId = :companyId " +
           "ORDER BY v.displayName")
    List<VendorSummaryDto> findByCompanyIdSummary(@Param("companyId") Long companyId);

    @Query("SELECT new com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto(" +
           "v.vendorId, " +
           "v.displayName, " +
           "COALESCE(vc.companyName, ''), " +
           "v.email, " + 
           "v.dueAmount, " +
           "v.isActive) " +
           "FROM Vendors v " +
           "LEFT JOIN v.vendorCompany vc " +
           "WHERE v.company.companyId = :companyId " +
           "AND v.vendorCompany IS NULL " +
           "ORDER BY v.displayName")
    List<VendorSummaryDto> findUnlinkedByCompanyIdSummary(@Param("companyId") Long companyId);
    
    @Query("SELECT new com.astraval.coreflow.modules.vendor.dto.VendorSummaryDto(" +
                  "v.vendorId, " +
                  "v.displayName, " +
                  "COALESCE(vc.companyName, ''), " +
                  "v.email, " +
                  "v.dueAmount, v.isActive) " +
                  "FROM Vendors v " +
                  "LEFT JOIN v.vendorCompany vc " +
                  "WHERE v.company.companyId = :companyId " +
                  "AND COALESCE(v.isActive, FALSE) = :isActive " +
                  "ORDER BY v.displayName")
    List<VendorSummaryDto> findByCompanyCompanyIdAndIsActiveOrderByDisplayName(
                  @Param("companyId") Long companyId, @Param("isActive") Boolean isActive);
    
    List<Vendors> findByCompanyCompanyIdOrderByDisplayName(Long companyId);

    Optional<Vendors> findByVendorIdAndCompanyCompanyId(
                  Long vendorId,
                  Long companyId);

    Optional<Vendors> findByCompanyCompanyIdAndVendorCompanyCompanyId(
                  Long companyId,
                  Long vendorCompanyId);


}
