package com.astraval.coreflow.modules.filestorage;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/file")
public class FileController {


  @GetMapping
  public ResponseEntity<Resource> getFileObject(@RequestParam String fsId) {
    try {
      return getFileObject(fsId);
    } catch (Exception e) {
      throw new RuntimeException("Invalid file path", e);
    }
  }

}
