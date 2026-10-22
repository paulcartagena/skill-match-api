package com.paulcartagena.skillmatchapi.user.repository;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.user.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    Optional<CandidateProfile> findByUser(User user);

    // Fetches skills up front so scoring every candidate doesn't trigger N+1 queries
    @Query("select distinct c from CandidateProfile c left join fetch c.skills s left join fetch s.skill")
    List<CandidateProfile> findAllWithSkills();
}
