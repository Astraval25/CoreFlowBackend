package com.astraval.coreflow.modules.filestorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileController {

  @Autowired
  private FileStorageService fileStorageService;

  @GetMapping
  public ResponseEntity<Resource> getFileObject(@RequestParam String fsId) {
    try {
      return fileStorageService.getFileObject(fsId);
    } catch (Exception e) {
      throw new RuntimeException("Invalid file path", e);
    }
  }
}
