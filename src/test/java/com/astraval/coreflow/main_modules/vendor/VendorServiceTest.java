package com.astraval.coreflow.main_modules.vendor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.astraval.coreflow.main_modules.address.AddressMapper;
import com.astraval.coreflow.main_modules.address.AddressService;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.companylink.CompanyLink;
import com.astraval.coreflow.main_modules.companylink.CompanyLinkRepository;
import com.astraval.coreflow.main_modules.customer.CustomerRepository;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.vendor.dto.CreateUpdateVendorDto;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private PartnerBalanceService partnerBalanceService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyLinkRepository companyLinkRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VendorService vendorService;

    private Companies ownerCompany;

    @BeforeEach
    void setUp() {
        ownerCompany = new Companies();
        ownerCompany.setCompanyId(3L);
        ownerCompany.setCompanyName("Buyer Co");

        lenient().when(companyLinkRepository.findByVendorVendorId(anyLong())).thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.findByVendorVendorIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.findByCustomerCompanyCompanyIdAndVendorCompanyCompanyId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(customerRepository.findByCompanyCompanyIdAndCustomerCompanyCompanyId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.save(any(CompanyLink.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createVendor_autoLinksWhenAccountExists() {
        CreateUpdateVendorDto request = createRequest("+91 99999 22222");

        Companies accountCompany = new Companies();
        accountCompany.setCompanyId(5L);
        accountCompany.setCompanyName("Seller Co");

        User user = new User();
        user.setDefaultCompany(accountCompany);

        when(companyRepository.findById(3L)).thenReturn(Optional.of(ownerCompany));
        when(userRepository.findActiveUserByPhoneKey("9999922222")).thenReturn(Optional.of(user));
        when(vendorRepository.save(any(Vendors.class))).thenAnswer(invocation -> {
            Vendors saved = invocation.getArgument(0);
            saved.setVendorId(88L);
            return saved;
        });

        Long vendorId = vendorService.createVendor(3L, request);

        assertEquals(88L, vendorId);
        ArgumentCaptor<Vendors> captor = ArgumentCaptor.forClass(Vendors.class);
        verify(vendorRepository).save(captor.capture());
        assertNotNull(captor.getValue().getVendorCompany());
        assertEquals(5L, captor.getValue().getVendorCompany().getCompanyId());
    }

    @Test
    void createVendor_keepsUnlinkedWhenNoAccount() {
        CreateUpdateVendorDto request = createRequest("8888877777");

        when(companyRepository.findById(3L)).thenReturn(Optional.of(ownerCompany));
        when(userRepository.findActiveUserByPhoneKey("8888877777")).thenReturn(Optional.empty());
        when(vendorRepository.save(any(Vendors.class))).thenAnswer(invocation -> {
            Vendors saved = invocation.getArgument(0);
            saved.setVendorId(89L);
            return saved;
        });

        Long vendorId = vendorService.createVendor(3L, request);

        assertEquals(89L, vendorId);
        ArgumentCaptor<Vendors> captor = ArgumentCaptor.forClass(Vendors.class);
        verify(vendorRepository).save(captor.capture());
        assertNull(captor.getValue().getVendorCompany());
    }

    @Test
    void updateVendor_autoLinksWhenCurrentlyUnlinked() {
        CreateUpdateVendorDto request = createRequest("+91 77777 66666");

        Vendors existing = new Vendors();
        existing.setVendorId(501L);
        existing.setCompany(ownerCompany);
        existing.setVendorCompany(null);

        Companies accountCompany = new Companies();
        accountCompany.setCompanyId(9L);
        accountCompany.setCompanyName("Remote Co");

        User user = new User();
        user.setDefaultCompany(accountCompany);

        when(vendorRepository.findById(501L)).thenReturn(Optional.of(existing));
        when(userRepository.findActiveUserByPhoneKey("7777766666")).thenReturn(Optional.of(user));
        when(vendorRepository.save(any(Vendors.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vendorService.updateVendor(3L, 501L, request);

        ArgumentCaptor<Vendors> captor = ArgumentCaptor.forClass(Vendors.class);
        verify(vendorRepository).save(captor.capture());
        assertNotNull(captor.getValue().getVendorCompany());
        assertEquals(9L, captor.getValue().getVendorCompany().getCompanyId());
    }

    private CreateUpdateVendorDto createRequest(String phone) {
        CreateUpdateVendorDto request = new CreateUpdateVendorDto();
        request.setVendorName("Test Vendor");
        request.setDisplayName("Test Vendor");
        request.setPhone(phone);
        request.setSameAsBillingAddress(false);
        return request;
    }
}
