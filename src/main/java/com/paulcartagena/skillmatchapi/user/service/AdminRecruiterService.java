package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminRecruiterService {

    private final UserRepository userRepository;

    public AdminRecruiterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<PendingRecruiterResponse> getPendingRecruiters() {
        return userRepository.findByRoleAndStatus(UserRole.RECRUITER, AccountStatus.PENDING_VERIFICATION)
                .stream()
                .map(user -> new PendingRecruiterResponse(user.getId(), user.getEmail()))
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
}
