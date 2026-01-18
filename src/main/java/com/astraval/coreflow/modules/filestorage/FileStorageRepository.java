package com.astraval.coreflow.modules.filestorage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, String> {
  Optional<FileStorage> findByFsId(String fsId);
}