package com.paulcartagena.skillmatchapi.company.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.company.repository.CompanyRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;

    @InjectMocks
    private CompanyService companyService;

    private User member;
    private Company acme;
    private CompanyRequest request;

    @BeforeEach
    void setUp() {
        member = new User();
        member.setId(1L);
        acme = new Company(7L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.STARTUP, null, null);
        request = new CompanyRequest("  Acme Labs ", "https://acme.dev", Industry.TECHNOLOGY, CompanySize.SMALL);
    }

    @Test
    void create_checksAndSavesTrimmedName() {
        when(companyRepository.existsByNameIgnoreCase("Acme Labs")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company company = companyService.create(request);

        assertThat(company.getName()).isEqualTo("Acme Labs");
        assertThat(company.getSize()).isEqualTo(CompanySize.SMALL);
    }

    @Test
    void create_throwsConflict_whenNameAlreadyExists() {
        when(companyRepository.existsByNameIgnoreCase("Acme Labs")).thenReturn(true);

        assertThatThrownBy(() -> companyService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(companyRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenCompanyDoesNotExist() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.findById(99L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_updatesCompany_whenUserBelongsToIt() {
        when(companyRepository.findById(7L)).thenReturn(Optional.of(acme));
        when(recruiterProfileRepository.findByUser(member)).thenReturn(Optional.of(recruiterOf(acme)));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("Acme Labs", 7L)).thenReturn(false);
        when(companyRepository.save(acme)).thenReturn(acme);

        CompanyResponse response = companyService.update(member, 7L, request);

        assertThat(response.getName()).isEqualTo("Acme Labs");
        assertThat(response.getWebsite()).isEqualTo("https://acme.dev");
    }

    @Test
    void update_throwsForbidden_whenUserBelongsToAnotherCompany() {
        Company other = new Company(8L, "Other", null, Industry.FINANCE, CompanySize.LARGE, null, null);
        when(companyRepository.findById(7L)).thenReturn(Optional.of(acme));
        when(recruiterProfileRepository.findByUser(member)).thenReturn(Optional.of(recruiterOf(other)));

        assertThatThrownBy(() -> companyService.update(member, 7L, request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(companyRepository, never()).save(any());
    }

    @Test
    void update_throwsConflict_whenRenamingToAnExistingName() {
        when(companyRepository.findById(7L)).thenReturn(Optional.of(acme));
        when(recruiterProfileRepository.findByUser(member)).thenReturn(Optional.of(recruiterOf(acme)));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("Acme Labs", 7L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.update(member, 7L, request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private RecruiterProfile recruiterOf(Company company) {
        RecruiterProfile profile = new RecruiterProfile();
        profile.setUser(member);
        profile.setCompany(company);
        return profile;
    }
}
