package com.paulcartagena.skillmatchapi.job.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.enums.CompanySize;
import com.paulcartagena.skillmatchapi.company.enums.Industry;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.job.dto.JobResponse;
import com.paulcartagena.skillmatchapi.job.dto.JobSkillRequest;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.entity.JobSkill;
import com.paulcartagena.skillmatchapi.job.enums.EmploymentType;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import com.paulcartagena.skillmatchapi.job.repository.JobRepository;
import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import com.paulcartagena.skillmatchapi.skill.repository.SkillRepository;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private JobService jobService;

    private User owner;
    private User otherUser;
    private RecruiterProfile recruiter;
    private Skill java;
    private Skill docker;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        otherUser = new User();
        otherUser.setId(2L);

        recruiter = new RecruiterProfile();
        recruiter.setUser(owner);
        recruiter.setCompany(new Company(1L, "Acme Inc.", null, Industry.TECHNOLOGY, CompanySize.SMALL, null, null));

        java = new Skill();
        java.setId(10L);
        java.setName("Java");

        docker = new Skill();
        docker.setId(20L);
        docker.setName("Docker");
    }

    @Test
    void create_savesActiveJobForRecruiter() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));
        when(skillRepository.findAllById(Set.of(10L))).thenReturn(List.of(java));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.create(owner, request("Backend Developer", required(10L)));

        assertThat(response.getTitle()).isEqualTo("Backend Developer");
        assertThat(response.getCompanyName()).isEqualTo("Acme Inc.");
        assertThat(response.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(response.getRequiredSkills()).containsExactly("Java");
    }

    @Test
    void create_throwsNotFound_whenRecruiterHasNoProfile() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.create(owner, request("Backend Developer", required(10L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenSomeSkillIdsDoNotExist() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));
        when(skillRepository.findAllById(Set.of(10L, 99L))).thenReturn(List.of(java));

        assertThatThrownBy(() -> jobService.create(owner, request("Backend Developer", required(10L), required(99L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void create_splitsRequiredAndDesiredSkills() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));
        when(skillRepository.findAllById(Set.of(10L, 20L))).thenReturn(List.of(java, docker));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.create(owner, request("Backend Developer", required(10L), desired(20L)));

        assertThat(response.getRequiredSkills()).containsExactly("Java");
        assertThat(response.getDesiredSkills()).containsExactly("Docker");
    }

    @Test
    void create_throwsBadRequest_whenNoSkillIsRequired() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));

        assertThatThrownBy(() -> jobService.create(owner, request("Backend Developer", desired(10L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenSkillIsListedTwice() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));

        assertThatThrownBy(() -> jobService.create(owner, request("Backend Developer", required(10L), desired(10L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void getJobs_returnsOnlyActiveJobs() {
        when(jobRepository.findByStatus(JobStatus.ACTIVE)).thenReturn(List.of(job(1L, JobStatus.ACTIVE)));

        List<JobResponse> jobs = jobService.getJobs();

        assertThat(jobs).extracting(JobResponse::getId).containsExactly(1L);
        verify(jobRepository).findByStatus(JobStatus.ACTIVE);
    }

    @Test
    void getJob_returnsActiveJob_toAnyUser() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.ACTIVE)));

        assertThat(jobService.getJob(otherUser, 1L).getId()).isEqualTo(1L);
    }

    @Test
    void getJob_returnsClosedJob_toItsOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.CLOSED)));

        assertThat(jobService.getJob(owner, 1L).getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void getJob_throwsNotFound_whenJobIsClosedAndUserIsNotOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.CLOSED)));

        assertThatThrownBy(() -> jobService.getJob(otherUser, 1L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getJob_throwsNotFound_whenJobDoesNotExist() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJob(owner, 99L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getMyJobs_returnsAllJobsOfRecruiter_includingClosed() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findByRecruiter(recruiter))
                .thenReturn(List.of(job(1L, JobStatus.ACTIVE), job(2L, JobStatus.CLOSED)));

        List<JobResponse> jobs = jobService.getMyJobs(owner);

        assertThat(jobs).extracting(JobResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    void getMyJobs_throwsNotFound_whenRecruiterHasNoProfile() {
        when(recruiterProfileRepository.findByUser(owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getMyJobs(owner))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_updatesFields_whenUserIsOwner() {
        Job existing = job(1L, JobStatus.ACTIVE);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.findAllById(Set.of(10L))).thenReturn(List.of(java));
        when(jobRepository.save(existing)).thenReturn(existing);

        JobResponse response = jobService.update(owner, 1L, request("Senior Backend Developer", required(10L)));

        assertThat(response.getTitle()).isEqualTo("Senior Backend Developer");
        assertThat(response.getWorkMode()).isEqualTo(WorkMode.REMOTE);
        assertThat(response.getRequiredSkills()).containsExactly("Java");
    }

    @Test
    void update_keepsExistingSkillRows_andReplacesRemovedOnes() {
        Job existing = job(1L, JobStatus.ACTIVE);
        JobSkill javaRow = existing.getSkills().iterator().next();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(skillRepository.findAllById(Set.of(10L, 20L))).thenReturn(List.of(java, docker));
        when(jobRepository.save(existing)).thenReturn(existing);

        JobResponse response = jobService.update(owner, 1L, request("Backend Developer", desired(10L), required(20L)));

        assertThat(existing.getSkills()).hasSize(2).contains(javaRow);
        assertThat(javaRow.isRequired()).isFalse();
        assertThat(response.getRequiredSkills()).containsExactly("Docker");
        assertThat(response.getDesiredSkills()).containsExactly("Java");
    }

    @Test
    void update_throwsForbidden_whenUserIsNotOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.ACTIVE)));

        assertThatThrownBy(() -> jobService.update(otherUser, 1L, request("Hijacked", required(10L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void update_throwsBadRequest_whenJobIsClosed() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.CLOSED)));

        assertThatThrownBy(() -> jobService.update(owner, 1L, request("New title", required(10L))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void close_setsStatusToClosed_whenUserIsOwner() {
        Job existing = job(1L, JobStatus.ACTIVE);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jobRepository.save(existing)).thenReturn(existing);

        JobResponse response = jobService.close(owner, 1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void close_throwsForbidden_whenUserIsNotOwner() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.ACTIVE)));

        assertThatThrownBy(() -> jobService.close(otherUser, 1L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void close_throwsBadRequest_whenJobIsAlreadyClosed() {
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job(1L, JobStatus.CLOSED)));

        assertThatThrownBy(() -> jobService.close(owner, 1L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private JobSkillRequest required(Long skillId) {
        return new JobSkillRequest(skillId, true);
    }

    private JobSkillRequest desired(Long skillId) {
        return new JobSkillRequest(skillId, false);
    }

    private JobRequest request(String title, JobSkillRequest... skills) {
        JobRequest request = new JobRequest();
        request.setTitle(title);
        request.setDescription("Build REST APIs with Spring Boot.");
        request.setLocation("Remote");
        request.setWorkMode(WorkMode.REMOTE);
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setSkills(List.of(skills));
        return request;
    }

    private Job job(Long id, JobStatus status) {
        Job job = new Job();
        job.setId(id);
        job.setRecruiter(recruiter);
        job.setTitle("Backend Developer");
        job.setWorkMode(WorkMode.HYBRID);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setStatus(status);
        job.getSkills().add(new JobSkill(1L, job, java, true));
        return job;
    }
}
