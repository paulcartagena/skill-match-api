package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminRecruiterService {

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public AdminRecruiterService(UserRepository userRepository,
                                 RecruiterProfileRepository recruiterProfileRepository) {
        this.userRepository = userRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    public List<PendingRecruiterResponse> getPendingRecruiters() {
        return userRepository.findByRoleAndStatus(UserRole.RECRUITER, AccountStatus.PENDING_VERIFICATION)
                .stream()
                .map(this::buildPendingResponse)
                .toList();
    }

    public void approveRecruiter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found."));

        if (user.getRole() != UserRole.RECRUITER) {
            throw ApiException.badRequest("User is not a recruiter.");
        }

        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    // Shows the company the recruiter registered with, so the admin approves that membership too
    private PendingRecruiterResponse buildPendingResponse(User user) {
        return recruiterProfileRepository.findByUser(user)
                .map(profile -> new PendingRecruiterResponse(
                        user.getId(), user.getEmail(), profile.getJobTitle(), CompanyResponse.from(profile.getCompany())))
                .orElseGet(() -> new PendingRecruiterResponse(user.getId(), user.getEmail(), null, null));
    }
}
