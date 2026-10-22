package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.service.CompanyService;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.RecruiterProfileResponse;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecruiterProfileService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyService companyService;

    public RecruiterProfileService(RecruiterProfileRepository recruiterProfileRepository,
                                   CompanyService companyService) {
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.companyService = companyService;
    }

    public RecruiterProfileResponse getMyProfile(User user) {
        RecruiterProfile profile = findByUserOrThrow(user);
        return buildResponse(profile);
    }

    /**
     * Creates the profile of a newly registered recruiter, joining an existing company
     * (companyId) or creating a new one (newCompany). Exactly one of them must be given.
     */
    public RecruiterProfile createProfile(User user, Long companyId, CompanyRequest newCompany, String jobTitle) {
        if ((companyId == null) == (newCompany == null)) {
            throw ApiException.badRequest("Provide either companyId to join a company or company to create one.");
        }

        Company company = companyId != null
                ? companyService.findById(companyId)
                : companyService.create(newCompany);

        RecruiterProfile profile = new RecruiterProfile();
        profile.setUser(user);
        profile.setCompany(company);
        profile.setJobTitle(jobTitle);
        return recruiterProfileRepository.save(profile);
    }

    public RecruiterProfileResponse update(User user, RecruiterProfileRequest request) {
        RecruiterProfile profile = findByUserOrThrow(user);
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
                profile.getJobTitle(),
                CompanyResponse.from(profile.getCompany())
        );
    }
}
