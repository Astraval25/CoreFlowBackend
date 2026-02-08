package com.astraval.coreflow.modules.payments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.modules.filestorage.FileStorageService;
import com.astraval.coreflow.modules.payments.dto.PaymentProofUploadResponse;

@Service
public class PaymentProofService {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private PaymentProofTextExtractor paymentProofTextExtractor;

    public PaymentProofUploadResponse uploadProof(Long companyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Payment proof file is required");
        }

        try {
            FileStorage fileStorage = fileStorageService.saveFile(file, "PAYMENT_PROOF", companyId.toString());
            FileStorage savedFile = fileStorageRepository.save(fileStorage);

            String extractedText = paymentProofTextExtractor.extractText(
                    savedFile.getFilePath(),
                    savedFile.getMimeType());
            String transactionId = paymentProofTextExtractor.extractTransactionId(extractedText);
            Double amount = paymentProofTextExtractor.extractAmount(extractedText);

            return new PaymentProofUploadResponse(savedFile.getFsId(), transactionId, amount, extractedText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload payment proof: " + e.getMessage(), e);
        }
    }
}
