package com.astraval.coreflow.modules.vendor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query(value = "SELECT COALESCE(fn_vendor_due_amount(:vendorId), 0.0)", nativeQuery = true)
    Double calculateDueAmount(@Param("vendorId") Long vendorId);

    @Query(value = """
           SELECT
             COALESCE((
               SELECT SUM(COALESCE(o.total_amount, 0.0))
               FROM order_details o
               JOIN vendors v ON v.vendor_id = o.vendor
               WHERE v.vendor_id = :vendorId
                 AND COALESCE(o.is_active, TRUE) = TRUE
                 AND o.buyer_company = v.comp_id
                 AND COALESCE(o.order_status, '') NOT IN (
                   'QUOTATION',
                   'QUOTATION_VIEWED',
                   'QUOTATION_ACCEPTED',
                   'QUOTATION_DECLINED'
                 )
             ), 0.0)
             -
             COALESCE((
               SELECT SUM(COALESCE(p.amount, 0.0))
               FROM payments p
               JOIN vendors v ON v.vendor_id = p.vendor
               WHERE v.vendor_id = :vendorId
                 AND COALESCE(p.is_active, TRUE) = TRUE
                 AND p.sender_comp = v.comp_id
                 AND COALESCE(p.payment_status, '') <> 'PAYMENT_DECLINED'
             ), 0.0)
           """, nativeQuery = true)
    Double calculateDueAmountFallback(@Param("vendorId") Long vendorId);

    @Modifying
    @Transactional
    @Query("UPDATE Vendors v SET v.dueAmount = :dueAmount WHERE v.vendorId = :vendorId")
    int updateDueAmount(@Param("vendorId") Long vendorId, @Param("dueAmount") Double dueAmount);


}
