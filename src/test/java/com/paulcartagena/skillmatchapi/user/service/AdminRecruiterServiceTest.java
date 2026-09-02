package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRecruiterServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminRecruiterService adminRecruiterService;

    @Test
    void getPendingRecruiters_mapsUsersToResponses() {
        User pending = new User();
        pending.setId(1L);
        pending.setEmail("recruiter@company.com");
        pending.setRole(UserRole.RECRUITER);
        pending.setStatus(AccountStatus.PENDING_VERIFICATION);

        when(userRepository.findByRoleAndStatus(UserRole.RECRUITER, AccountStatus.PENDING_VERIFICATION))
                .thenReturn(List.of(pending));

        List<PendingRecruiterResponse> result = adminRecruiterService.getPendingRecruiters();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getEmail()).isEqualTo("recruiter@company.com");
    }

    @Test
    void approveRecruiter_activatesRecruiterAccount() {
        User recruiter = new User();
        recruiter.setId(1L);
        recruiter.setRole(UserRole.RECRUITER);
        recruiter.setStatus(AccountStatus.PENDING_VERIFICATION);

        when(userRepository.findById(1L)).thenReturn(Optional.of(recruiter));

        adminRecruiterService.approveRecruiter(1L);

        assertThat(recruiter.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(userRepository).save(recruiter);
    }

    @Test
    void approveRecruiter_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminRecruiterService.approveRecruiter(1L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(userRepository, never()).save(any());
    }

    @Test
    void approveRecruiter_throwsBadRequest_whenUserIsNotRecruiter() {
        User candidate = new User();
        candidate.setId(1L);
        candidate.setRole(UserRole.CANDIDATE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> adminRecruiterService.approveRecruiter(1L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }
}
