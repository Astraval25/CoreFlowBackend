package com.astraval.coreflow.modules.advertisement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    Page<Advertisement> findByPlacementAndIsActiveTrueOrderByCreatedDtDesc(String placement, Pageable pageable);

    Page<Advertisement> findByIsActiveTrueOrderByCreatedDtDesc(Pageable pageable);

    Page<Advertisement> findAllByOrderByCreatedDtDesc(Pageable pageable);
}
