package com.astraval.coreflow.main_modules.companies;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.main_modules.companies.dto.CompanyDetailDto;
import com.astraval.coreflow.main_modules.companies.dto.CompanySummaryDto;
import com.astraval.coreflow.main_modules.companies.dto.CreateUpdateCompanyDto;
import com.astraval.coreflow.main_modules.filestorage.FileStorage;
import com.astraval.coreflow.main_modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.main_modules.filestorage.FileStorageService;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMapRepository;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    public List<Companies> getAllCompanies() {
        return companyRepository.findAll();
    }

    public List<CompanySummaryDto> getCompaniesByUserId() {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);
        return companyRepository.findCompaniesByUserId(userId);
    }

    public List<CompanySummaryDto> getActiveCompaniesByUserId() {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);
        return companyRepository.findActiveCompaniesByUserId(userId);
    }

    public CompanyDetailDto getCompanyById(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        return new CompanyDetailDto(
                company.getCompanyId(),
                company.getCompanyName(),
                company.getIndustry(),
                company.getPan(),
                company.getGstNo(),
                company.getHsnCode(),
                company.getShortName(),
                company.getFsId(),
                company.getIsActive());
    }

    @Transactional
    public Long createCompany(CreateUpdateCompanyDto request) {
        String currentUserId = securityUtil.getCurrentSub();
        Long userId = Long.parseLong(currentUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Companies company = new Companies();
        company.setCompanyName(request.getCompanyName());
        company.setIndustry(request.getIndustry());
        company.setPan(request.getPan());
        company.setGstNo(request.getGstNo());
        company.setHsnCode(request.getHsnCode());
        company.setShortName(request.getShortName());

        Companies savedCompany = companyRepository.save(company);

        // Map company with user
        UserCompanyMap userCompanyMap = new UserCompanyMap();
        userCompanyMap.setUser(user);
        userCompanyMap.setCompany(savedCompany);
        userCompanyMapRepository.save(userCompanyMap);

        return savedCompany.getCompanyId();
    }

    @Transactional
    public void updateCompany(Long companyId, CreateUpdateCompanyDto request) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        company.setCompanyName(request.getCompanyName());
        company.setIndustry(request.getIndustry());
        company.setPan(request.getPan());
        company.setGstNo(request.getGstNo());
        company.setHsnCode(request.getHsnCode());
        company.setShortName(request.getShortName());

        companyRepository.save(company);
    }

    @Transactional
    public String uploadCompanyLogo(Long companyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Logo file is required");
        }
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        try {
            FileStorage fileStorage = fileStorageService.saveFile(file, "COMPANY_LOGO", companyId.toString());
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            company.setFsId(savedFile.getFsId());
            companyRepository.save(company);
            return savedFile.getFsId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload company logo: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deactivateCompany(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        company.setIsActive(false);
        companyRepository.save(company);
    }

    @Transactional
    public void activateCompany(Long companyId) {
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
        company.setIsActive(true);
        companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new RuntimeException("Company not found with ID: " + companyId);
        }
        companyRepository.deleteById(companyId);
    }
}
