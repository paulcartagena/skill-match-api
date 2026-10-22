package com.paulcartagena.skillmatchapi.company.repository;

import com.paulcartagena.skillmatchapi.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<Company> findAllByOrderByNameAsc();
}
