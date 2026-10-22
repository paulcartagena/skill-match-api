package com.paulcartagena.skillmatchapi.user.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.enums.SkillLevel;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateProfileResponse;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillResponse;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.entity.CandidateSkill;
import com.paulcartagena.skillmatchapi.user.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final SkillRepository skillRepository;

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
                                   SkillRepository skillRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.skillRepository = skillRepository;
    }

    public CandidateProfileResponse getMyProfile(User user) {
        CandidateProfile profile = findByUserOrThrow(user);
        return buildResponse(profile);
    }

    public CandidateProfileResponse createOrUpdate(User user, CandidateProfileRequest request) {
        CandidateProfile profile = candidateProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    CandidateProfile newProfile = new CandidateProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setFullName(request.getFullName());
        profile.setBio(request.getBio());
        profile.setLocation(request.getLocation());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setPhoneNumber(request.getPhoneNumber());

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return buildResponse(saved);
    }

    public CandidateProfileResponse setSkill(User user, Long skillId, SkillLevel level) {
        CandidateProfile profile = findByUserOrThrow(user);
        Skill skill = findSkillOrThrow(skillId);

        // Adding a skill the candidate already has just updates its level
        CandidateSkill candidateSkill = findCandidateSkill(profile, skill.getId())
                .orElseGet(() -> {
                    CandidateSkill newSkill = new CandidateSkill();
                    newSkill.setCandidate(profile);
                    newSkill.setSkill(skill);
                    profile.getSkills().add(newSkill);
                    return newSkill;
                });
        candidateSkill.setLevel(level);

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return buildResponse(saved);
    }

    public CandidateProfileResponse removeSkill(User user, Long skillId) {
        CandidateProfile profile = findByUserOrThrow(user);
        Skill skill = findSkillOrThrow(skillId);

        findCandidateSkill(profile, skill.getId()).ifPresent(profile.getSkills()::remove);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return buildResponse(saved);
    }

    private CandidateProfile findByUserOrThrow(User user) {
        return candidateProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.notFound("Profile not found. Complete your profile first."));
    }

    private Skill findSkillOrThrow(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> ApiException.notFound("Skill not found."));
    }

    private Optional<CandidateSkill> findCandidateSkill(CandidateProfile profile, Long skillId) {
        return profile.getSkills().stream()
                .filter(candidateSkill -> candidateSkill.getSkill().getId().equals(skillId))
                .findFirst();
    }

    private CandidateProfileResponse buildResponse(CandidateProfile profile) {
        Set<CandidateSkillResponse> skillResponses = profile.getSkills().stream()
                .map(CandidateSkillResponse::from)
                .collect(Collectors.toSet());

        return new CandidateProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getBio(),
                profile.getLocation(),
                profile.getYearsOfExperience(),
                profile.getPhoneNumber(),
                skillResponses
        );
    }
}