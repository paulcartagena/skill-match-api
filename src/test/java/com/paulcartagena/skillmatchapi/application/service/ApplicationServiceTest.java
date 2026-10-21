package com.paulcartagena.skillmatchapi.application.service;

import com.paulcartagena.skillmatchapi.application.dto.ApplicationResponse;
import com.paulcartagena.skillmatchapi.application.entity.Application;
import com.paulcartagena.skillmatchapi.application.enums.ApplicationStatus;
import com.paulcartagena.skillmatchapi.application.repository.ApplicationRepository;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private CandidateProfileRepository candidateProfileRepository;
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User candidateUser;
    private User recruiterUser;
    private User otherRecruiterUser;
    private CandidateProfile candidate;
    private Job job;

    @BeforeEach
    void setUp() {
        candidateUser = new User();
        candidateUser.setId(1L);
        recruiterUser = new User();
        recruiterUser.setId(2L);
        otherRecruiterUser = new User();
        otherRecruiterUser.setId(3L);

        Skill java = new Skill();
        java.setName("Java");

        candidate = new CandidateProfile();
        candidate.setId(10L);
        candidate.setUser(candidateUser);
        candidate.setFullName("Ana Pérez");
        candidate.setSkills(Set.of(java));

        RecruiterProfile recruiter = new RecruiterProfile();
        recruiter.setUser(recruiterUser);
        recruiter.setCompanyName("Acme Inc.");

        job = new Job();
        job.setId(100L);
        job.setRecruiter(recruiter);
        job.setTitle("Backend Developer");
        job.setStatus(JobStatus.ACTIVE);
    }

    @Test
    void apply_createsApplicationWithAppliedStatus() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateAndJob(candidate, job)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.apply(candidateUser, 100L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getJobTitle()).isEqualTo("Backend Developer");
        assertThat(response.getCompanyName()).isEqualTo("Acme Inc.");
        assertThat(response.getCandidateName()).isEqualTo("Ana Pérez");
        assertThat(response.getCandidateSkills()).containsExactly("Java");
    }

    @Test
    void apply_throwsNotFound_whenCandidateHasNoProfile() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.empty());

        assertStatus(() -> applicationService.apply(candidateUser, 100L), HttpStatus.NOT_FOUND);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void apply_throwsNotFound_whenJobDoesNotExist() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertStatus(() -> applicationService.apply(candidateUser, 999L), HttpStatus.NOT_FOUND);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void apply_throwsBadRequest_whenJobIsNotActive() {
        job.setStatus(JobStatus.CLOSED);
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        assertStatus(() -> applicationService.apply(candidateUser, 100L), HttpStatus.BAD_REQUEST);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void apply_throwsConflict_whenCandidateAlreadyApplied() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByCandidateAndJob(candidate, job)).thenReturn(true);

        assertStatus(() -> applicationService.apply(candidateUser, 100L), HttpStatus.CONFLICT);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void getMyApplications_returnsCandidateApplications() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.of(candidate));
        when(applicationRepository.findByCandidate(candidate))
                .thenReturn(List.of(application(1L, ApplicationStatus.APPLIED)));

        List<ApplicationResponse> applications = applicationService.getMyApplications(candidateUser);

        assertThat(applications).extracting(ApplicationResponse::getId).containsExactly(1L);
    }

    @Test
    void getMyApplications_throwsNotFound_whenCandidateHasNoProfile() {
        when(candidateProfileRepository.findByUser(candidateUser)).thenReturn(Optional.empty());

        assertStatus(() -> applicationService.getMyApplications(candidateUser), HttpStatus.NOT_FOUND);
    }

    @Test
    void getJobApplications_returnsApplicants_whenUserOwnsJob() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJob(job)).thenReturn(List.of(
                application(1L, ApplicationStatus.APPLIED),
                application(2L, ApplicationStatus.REVIEWED)));

        List<ApplicationResponse> applications = applicationService.getJobApplications(recruiterUser, 100L);

        assertThat(applications).extracting(ApplicationResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void getJobApplications_throwsForbidden_whenUserDoesNotOwnJob() {
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        assertStatus(() -> applicationService.getJobApplications(otherRecruiterUser, 100L), HttpStatus.FORBIDDEN);
        verify(applicationRepository, never()).findByJob(any());
    }

    @Test
    void getJobApplications_throwsNotFound_whenJobDoesNotExist() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertStatus(() -> applicationService.getJobApplications(recruiterUser, 999L), HttpStatus.NOT_FOUND);
    }

    @Test
    void updateStatus_changesStatus_whenTransitionIsValid() {
        Application existing = application(1L, ApplicationStatus.APPLIED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);

        ApplicationResponse response =
                applicationService.updateStatus(recruiterUser, 1L, ApplicationStatus.REVIEWED);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REVIEWED);
    }

    @Test
    void updateStatus_throwsBadRequest_whenTransitionIsInvalid() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application(1L, ApplicationStatus.REJECTED)));

        assertStatus(() -> applicationService.updateStatus(recruiterUser, 1L, ApplicationStatus.ACCEPTED),
                HttpStatus.BAD_REQUEST);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateStatus_throwsForbidden_whenUserDoesNotOwnJob() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application(1L, ApplicationStatus.APPLIED)));

        assertStatus(() -> applicationService.updateStatus(otherRecruiterUser, 1L, ApplicationStatus.REVIEWED),
                HttpStatus.FORBIDDEN);
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void updateStatus_throwsNotFound_whenApplicationDoesNotExist() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertStatus(() -> applicationService.updateStatus(recruiterUser, 999L, ApplicationStatus.REVIEWED),
                HttpStatus.NOT_FOUND);
    }

    private Application application(Long id, ApplicationStatus status) {
        Application application = new Application();
        application.setId(id);
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(status);
        return application;
    }

    private void assertStatus(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call)
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(status);
    }
}
