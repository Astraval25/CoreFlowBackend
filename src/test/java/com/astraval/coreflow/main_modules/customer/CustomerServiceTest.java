package com.astraval.coreflow.main_modules.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.astraval.coreflow.main_modules.customer.dto.CreateUpdateCustomerDto;
import com.astraval.coreflow.main_modules.customer.dto.CustomerContactLookupRequest;
import com.astraval.coreflow.main_modules.customer.dto.CustomerContactLookupResultDto;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.vendor.VendorRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

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
    private VendorRepository vendorRepository;

    @InjectMocks
    private CustomerService customerService;

    private Companies ownerCompany;

    @BeforeEach
    void setUp() {
        ownerCompany = new Companies();
        ownerCompany.setCompanyId(1L);
        ownerCompany.setCompanyName("Owner Co");

        lenient().when(companyLinkRepository.findByCustomerCustomerId(anyLong())).thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.findByCustomerCustomerIdAndIsActiveTrue(anyLong())).thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.findByCustomerCompanyCompanyIdAndVendorCompanyCompanyId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(vendorRepository.findByCompanyCompanyIdAndVendorCompanyCompanyId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(companyLinkRepository.save(any(CompanyLink.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createCustomer_autoLinksWhenAccountExists() {
        CreateUpdateCustomerDto request = createRequest("+91 99999 11111");

        Companies accountCompany = new Companies();
        accountCompany.setCompanyId(2L);
        accountCompany.setCompanyName("Account Co");

        User user = new User();
        user.setDefaultCompany(accountCompany);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(ownerCompany));
        when(customerRepository.findFirstByCompanyIdAndPhoneKey(1L, "9999911111")).thenReturn(Optional.empty());
        when(userRepository.findActiveUserByPhoneKey("9999911111")).thenReturn(Optional.of(user));
        when(customerRepository.save(any(Customers.class))).thenAnswer(invocation -> {
            Customers saved = invocation.getArgument(0);
            saved.setCustomerId(101L);
            return saved;
        });

        Long id = customerService.createCustomer(1L, request);

        assertEquals(101L, id);
        ArgumentCaptor<Customers> customerCaptor = ArgumentCaptor.forClass(Customers.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(2L, customerCaptor.getValue().getCustomerCompany().getCompanyId());
        verify(partnerBalanceService).refreshCustomerDueAmount(101L);
    }

    @Test
    void createCustomer_keepsLocalWhenNoAccountMatch() {
        CreateUpdateCustomerDto request = createRequest("+91 88888 77777");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(ownerCompany));
        when(customerRepository.findFirstByCompanyIdAndPhoneKey(1L, "8888877777")).thenReturn(Optional.empty());
        when(userRepository.findActiveUserByPhoneKey("8888877777")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customers.class))).thenAnswer(invocation -> {
            Customers saved = invocation.getArgument(0);
            saved.setCustomerId(102L);
            return saved;
        });

        Long id = customerService.createCustomer(1L, request);

        assertEquals(102L, id);
        ArgumentCaptor<Customers> customerCaptor = ArgumentCaptor.forClass(Customers.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertNull(customerCaptor.getValue().getCustomerCompany());
    }

    @Test
    void createCustomer_duplicatePhoneThrowsActionableException() {
        CreateUpdateCustomerDto request = createRequest("+91 99999 11111");
        Customers existing = new Customers();
        existing.setCustomerId(55L);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(ownerCompany));
        when(customerRepository.findFirstByCompanyIdAndPhoneKey(1L, "9999911111"))
                .thenReturn(Optional.of(existing));

        DuplicateCustomerPhoneException exception = assertThrows(
                DuplicateCustomerPhoneException.class,
                () -> customerService.createCustomer(1L, request));

        assertEquals(55L, exception.getExistingCustomerId());
        assertEquals("9999911111", exception.getPhoneKey());
    }

    @Test
    void contactLookup_returnsAccountAndDuplicateInfo() {
        CustomerContactLookupRequest request = new CustomerContactLookupRequest();
        request.setPhones(List.of("+91 99999 11111", "123", "8888888888"));

        Companies accountCompany = new Companies();
        accountCompany.setCompanyId(7L);
        accountCompany.setCompanyName("Buyer Co");

        User user = new User();
        user.setDefaultCompany(accountCompany);

        Customers existingCustomer = new Customers();
        existingCustomer.setCustomerId(77L);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(ownerCompany));
        when(userRepository.findActiveUserByPhoneKey("9999911111")).thenReturn(Optional.of(user));
        when(customerRepository.findFirstByCompanyIdAndPhoneKey(1L, "9999911111"))
                .thenReturn(Optional.of(existingCustomer));
        when(userRepository.findActiveUserByPhoneKey("8888888888")).thenReturn(Optional.empty());
        when(customerRepository.findFirstByCompanyIdAndPhoneKey(1L, "8888888888"))
                .thenReturn(Optional.empty());

        List<CustomerContactLookupResultDto> results = customerService.contactLookup(1L, request);

        assertEquals(3, results.size());
        CustomerContactLookupResultDto first = results.get(0);
        assertEquals("9999911111", first.getPhoneKey());
        assertTrue(first.getHasAccount());
        assertEquals(7L, first.getAccountCompanyId());
        assertEquals(77L, first.getExistingCustomerId());

        CustomerContactLookupResultDto second = results.get(1);
        assertFalse(second.getValidPhone());
        assertFalse(second.getHasAccount());

        CustomerContactLookupResultDto third = results.get(2);
        assertTrue(third.getValidPhone());
        assertFalse(third.getHasAccount());
        assertNull(third.getExistingCustomerId());
    }

    @Test
    void linkCustomerByPhone_linksUnlinkedCustomer() {
        Customers customer = new Customers();
        customer.setCustomerId(20L);
        customer.setPhone("+91 70000 12345");
        customer.setCustomerCompany(null);
        customer.setCompany(ownerCompany);

        Companies accountCompany = new Companies();
        accountCompany.setCompanyId(3L);
        accountCompany.setCompanyName("Buyer Co");

        User user = new User();
        user.setDefaultCompany(accountCompany);

        when(customerRepository.findByCustomerIdAndCompanyCompanyId(20L, 1L)).thenReturn(Optional.of(customer));
        when(userRepository.findActiveUserByPhoneKey("7000012345")).thenReturn(Optional.of(user));
        when(customerRepository.save(any(Customers.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customers linked = customerService.linkCustomerByPhone(1L, 20L);

        assertNotNull(linked.getCustomerCompany());
        assertEquals(3L, linked.getCustomerCompany().getCompanyId());
    }

    @Test
    void linkCustomerByPhone_failsWhenAlreadyLinked() {
        Customers customer = new Customers();
        customer.setCustomerId(20L);
        customer.setPhone("+91 70000 12345");
        customer.setCompany(ownerCompany);

        Companies linkedCompany = new Companies();
        linkedCompany.setCompanyId(9L);
        customer.setCustomerCompany(linkedCompany);

        when(customerRepository.findByCustomerIdAndCompanyCompanyId(20L, 1L)).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.linkCustomerByPhone(1L, 20L));
        assertTrue(exception.getMessage().contains("already linked"));
    }

    @Test
    void linkCustomerByPhone_failsWhenNoAccountFound() {
        Customers customer = new Customers();
        customer.setCustomerId(20L);
        customer.setPhone("+91 70000 12345");
        customer.setCompany(ownerCompany);
        customer.setCustomerCompany(null);

        when(customerRepository.findByCustomerIdAndCompanyCompanyId(20L, 1L)).thenReturn(Optional.of(customer));
        when(userRepository.findActiveUserByPhoneKey("7000012345")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerService.linkCustomerByPhone(1L, 20L));
        assertTrue(exception.getMessage().contains("No CoreFlow account"));
    }

    private CreateUpdateCustomerDto createRequest(String phone) {
        CreateUpdateCustomerDto request = new CreateUpdateCustomerDto();
        request.setCustomerName("Test Customer");
        request.setDisplayName("Test");
        request.setPhone(phone);
        request.setSameAsBillingAddress(false);
        return request;
    }
}
