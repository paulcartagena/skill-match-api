package com.paulcartagena.skillmatchapi.job.repository;

import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.job.enums.JobStatus;
import com.paulcartagena.skillmatchapi.job.enums.WorkMode;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByRecruiter(RecruiterProfile recruiter);
    List<Job> findByWorkMode(WorkMode workMode);
    List<Job> findByStatus(JobStatus status);

    // Fetches skills and recruiter up front so scoring many jobs doesn't trigger N+1 queries
    @EntityGraph(attributePaths = {"skills", "skills.skill", "recruiter"})
    List<Job> findWithSkillsByStatus(JobStatus status);
}
