package com.paulcartagena.skillmatchapi.matching.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.matching.dto.CandidateMatchResponse;
import com.paulcartagena.skillmatchapi.matching.dto.JobMatchResponse;
import com.paulcartagena.skillmatchapi.matching.service.MatchScorer.MatchResult;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.CandidateProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private MatchScorer matchScorer;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;

    @InjectMocks
    private MatchingService matchingService;

    private User candidateUser;
    private User recruiterUser;
    private RecruiterProfile recruiter;

    @BeforeEach
    void setUp() {
        candidateUser = new User();
        candidateUser.setId(1L);
        recruiterUser = new User();
        recruiterUser.setId(2L);

        recruiter = new RecruiterProfile();
        recruiter.setUser(recruiterUser);
        recruiter.setCompanyName("Acme Inc.");
    }

    @Test
    void getJobMatches_returnsActiveJobsSortedByScoreDescending() {
        CandidateProfile candidate = candidate(10L);
        Job low = job(100L);
        Job high = job(200L);

        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findWithSkillsByStatus(JobStatus.ACTIVE)).thenReturn(List.of(low, high));
        when(matchScorer.score(candidate, low)).thenReturn(result(30));
        when(matchScorer.score(candidate, high)).thenReturn(result(85));

        List<JobMatchResponse> matches = matchingService.getJobMatches(candidateUser);

        assertThat(matches).extracting(JobMatchResponse::getJobId).containsExactly(200L, 100L);
        assertThat(matches.getFirst().getCompanyName()).isEqualTo("Acme Inc.");
    }

    @Test
    void getJobMatches_throwsNotFound_whenCandidateHasNoProfile() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getJobMatches(candidateUser))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getCandidateMatches_returnsCandidatesSortedByScoreDescending() {
        Job job = job(100L);
        CandidateProfile low = candidate(10L);
        CandidateProfile high = candidate(20L);

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(candidateProfileRepository.findAllWithSkills()).thenReturn(List.of(low, high));
        when(matchScorer.score(low, job)).thenReturn(result(10));
        when(matchScorer.score(high, job)).thenReturn(result(90));

        List<CandidateMatchResponse> matches = matchingService.getCandidateMatches(recruiterUser, 100L);

        assertThat(matches).extracting(CandidateMatchResponse::getCandidateId).containsExactly(20L, 10L);
        assertThat(matches).extracting(CandidateMatchResponse::getScore).containsExactly(90, 10);
    }

    @Test
    void getCandidateMatches_throwsForbidden_whenUserIsNotJobOwner() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job(100L)));

        assertThatThrownBy(() -> matchingService.getCandidateMatches(candidateUser, 100L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(candidateProfileRepository, never()).findAllWithSkills();
    }

    @Test
    void getCandidateMatches_throwsNotFound_whenJobDoesNotExist() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.getCandidateMatches(recruiterUser, 999L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private MatchResult result(int score) {
        return new MatchResult(score, Set.of(), Set.of());
    }

    private Job job(Long id) {
        Job job = new Job();
        job.setId(id);
        job.setRecruiter(recruiter);
        job.setTitle("Backend Developer");
        return job;
    }

    private CandidateProfile candidate(Long id) {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setId(id);
        candidate.setFullName("Candidate " + id);
        return candidate;
    }
}
