package com.paulcartagena.skillmatchapi.company.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.company.dto.CompanyRequest;
import com.paulcartagena.skillmatchapi.company.dto.CompanyResponse;
import com.paulcartagena.skillmatchapi.company.entity.Company;
import com.paulcartagena.skillmatchapi.company.repository.CompanyRepository;
import com.paulcartagena.skillmatchapi.exception.ApiException;
import com.paulcartagena.skillmatchapi.user.entity.RecruiterProfile;
import com.paulcartagena.skillmatchapi.user.repository.RecruiterProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public CompanyService(CompanyRepository companyRepository,
                          RecruiterProfileRepository recruiterProfileRepository) {
        this.companyRepository = companyRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    public List<CompanyResponse> getCompanies() {
        return companyRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CompanyResponse::from)
                .toList();
    }

    public CompanyResponse getCompany(Long id) {
        return CompanyResponse.from(findById(id));
    }

    public Company create(CompanyRequest request) {
        if (companyRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw ApiException.conflict("A company with this name already exists. Join it with its companyId.");
        }

        Company company = new Company();
        applyFields(company, request);
        return companyRepository.save(company);
    }

    public CompanyResponse update(User user, Long id, CompanyRequest request) {
        Company company = findById(id);

        RecruiterProfile recruiter = recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> ApiException.forbidden("You can only edit your own company."));
        if (!recruiter.getCompany().getId().equals(company.getId())) {
            throw ApiException.forbidden("You can only edit your own company.");
        }
        if (companyRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), id)) {
            throw ApiException.conflict("A company with this name already exists.");
        }

        applyFields(company, request);
        return CompanyResponse.from(companyRepository.save(company));
    }

    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Company not found."));
    }

    private void applyFields(Company company, CompanyRequest request) {
        company.setName(request.getName().trim());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
    }
}
