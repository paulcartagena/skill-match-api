package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;

    public RecruiterProfileService(RecruiterProfileRepository recruiterProfileRepository) {
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    public RecruiterProfileResponse getMyProfile(User user) {
        RecruiterProfile profile = findByUserOrThrow(user);
        return buildResponse(profile);
    }

    public RecruiterProfileResponse createOrUpdate(User user, RecruiterProfileRequest request) {
        RecruiterProfile profile = recruiterProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    RecruiterProfile newProfile = new RecruiterProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setCompanyName(request.getCompanyName());
        profile.setCompanyWebsite(request.getCompanyWebsite());
        profile.setIndustry(request.getIndustry());
        profile.setCompanySize(request.getCompanySize());
        profile.setJobTitle(request.getJobTitle());

        RecruiterProfile saved = recruiterProfileRepository.save(profile);
        return buildResponse(saved);
    }

    private RecruiterProfile findByUserOrThrow(User user) {
        return recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Profile not found. Complete your profile first."));
    }

    private RecruiterProfileResponse buildResponse(RecruiterProfile profile) {
        return new RecruiterProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getCompanyWebsite(),
                profile.getIndustry(),
                profile.getCompanySize(),
                profile.getJobTitle()
        );
    }
}