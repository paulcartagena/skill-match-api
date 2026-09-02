package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.enums.CompanySize;
import com.paulcartagena.skillmatchapi.user.enums.Industry;
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

    @InjectMocks
    private RecruiterProfileService recruiterProfileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
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
    void createOrUpdate_createsNewProfile_whenNoneExists() {
        RecruiterProfileRequest request = new RecruiterProfileRequest();
        request.setCompanyName("Acme Inc.");
        request.setIndustry(Industry.TECHNOLOGY);
        request.setCompanySize(CompanySize.STARTUP);

        when(recruiterProfileRepository.findByUser(user)).thenReturn(Optional.empty());
        when(recruiterProfileRepository.save(any(RecruiterProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecruiterProfileResponse response = recruiterProfileService.createOrUpdate(user, request);

        assertThat(response.getCompanyName()).isEqualTo("Acme Inc.");
        verify(recruiterProfileRepository).save(argThat(profile -> profile.getUser() == user));
    }

    @Test
    void createOrUpdate_updatesExistingProfile_insteadOfCreatingNew() {
        RecruiterProfile existing = new RecruiterProfile();
        existing.setId(5L);
        existing.setUser(user);
        existing.setCompanyName("Old Company");

        RecruiterProfileRequest request = new RecruiterProfileRequest();
        request.setCompanyName("New Company");
        request.setIndustry(Industry.FINANCE);
        request.setCompanySize(CompanySize.LARGE);

        when(recruiterProfileRepository.findByUser(user)).thenReturn(Optional.of(existing));
        when(recruiterProfileRepository.save(any(RecruiterProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecruiterProfileResponse response = recruiterProfileService.createOrUpdate(user, request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getCompanyName()).isEqualTo("New Company");
    }
}
