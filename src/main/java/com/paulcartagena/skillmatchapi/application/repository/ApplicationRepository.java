package com.paulcartagena.skillmatchapi.application.repository;

import com.paulcartagena.skillmatchapi.application.entity.Application;
import com.paulcartagena.skillmatchapi.job.entity.Job;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByCandidateAndJob(CandidateProfile candidate, Job job);
    List<Application> findByCandidate(CandidateProfile candidate);
    List<Application> findByJob(Job job);
}
