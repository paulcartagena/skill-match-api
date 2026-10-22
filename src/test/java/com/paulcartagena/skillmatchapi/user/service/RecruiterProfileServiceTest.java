package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.company.service.CompanyService;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
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
class RecruiterProfileServiceTest {

    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;
    @Mock
    private CompanyService companyService;

    @InjectMocks
    private RecruiterProfileService recruiterProfileService;

    private User user;
    private Company acme;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        acme = new Company(7L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.STARTUP, null, null);
    }

    @Test
    void getMyProfile_throwsNotFound_whenProfileDoesNotExist() {
        when(recruiterProfileRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recruiterProfileService.getMyProfile(user))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getMyProfile_includesCompany() {
        RecruiterProfile profile = new RecruiterProfile();
        profile.setId(5L);
        profile.setUser(user);
        profile.setCompany(acme);
        when(recruiterProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        RecruiterProfileResponse response = recruiterProfileService.getMyProfile(user);

        assertThat(response.getCompany().getName()).isEqualTo("Acme Inc.");
    }

    @Test
    void createProfile_joinsExistingCompany_whenCompanyIdIsGiven() {
        when(companyService.findById(7L)).thenReturn(acme);
        when(recruiterProfileRepository.save(any(RecruiterProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecruiterProfile profile = recruiterProfileService.createProfile(user, 7L, null, "Tech Lead");

        assertThat(profile.getUser()).isSameAs(user);
        assertThat(profile.getCompany()).isSameAs(acme);
        assertThat(profile.getJobTitle()).isEqualTo("Tech Lead");
        verify(companyService, never()).create(any());
    }

    @Test
    void createProfile_createsNewCompany_whenCompanyDataIsGiven() {
        CompanyRequest newCompany = new CompanyRequest("Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.STARTUP);
        when(companyService.create(newCompany)).thenReturn(acme);
        when(recruiterProfileRepository.save(any(RecruiterProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecruiterProfile profile = recruiterProfileService.createProfile(user, null, newCompany, null);

        assertThat(profile.getCompany()).isSameAs(acme);
    }

    @Test
    void createProfile_throwsBadRequest_whenBothOrNeitherCompanyOptionIsGiven() {
        CompanyRequest newCompany = new CompanyRequest("Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.STARTUP);

        assertThatThrownBy(() -> recruiterProfileService.createProfile(user, 7L, newCompany, null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> recruiterProfileService.createProfile(user, null, null, null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(recruiterProfileRepository, never()).save(any());
    }

    @Test
    void update_changesJobTitle_butNotCompany() {
        RecruiterProfile existing = new RecruiterProfile();
        existing.setId(5L);
        existing.setUser(user);
        existing.setCompany(acme);
        existing.setJobTitle("Recruiter");

        RecruiterProfileRequest request = new RecruiterProfileRequest();
        request.setJobTitle("Head of Talent");

        when(recruiterProfileRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(recruiterProfileRepository.save(existing)).thenReturn(existing);

        RecruiterProfileResponse response = recruiterProfileService.update(user, request);

        assertThat(response.getJobTitle()).isEqualTo("Head of Talent");
        assertThat(response.getCompany().getId()).isEqualTo(7L);
    }
}
