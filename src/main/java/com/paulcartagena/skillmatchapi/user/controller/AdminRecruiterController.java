package com.paulcartagena.skillmatchapi.user.controller;

import com.paulcartagena.skillmatchapi.user.dto.PendingRecruiterResponse;
import com.paulcartagena.skillmatchapi.user.service.AdminRecruiterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recruiters")
@Tag(name = "Admin - Recruiters")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecruiterController {

    private final AdminRecruiterService adminRecruiterService;

    public AdminRecruiterController(AdminRecruiterService adminRecruiterService) {
        this.adminRecruiterService = adminRecruiterService;
    }

    @GetMapping("/pending")
    public List<PendingRecruiterResponse> getPendingRecruiters() {
        return adminRecruiterService.getPendingRecruiters();
    }

    @PatchMapping("/{userId}/approve")
    public void approveRecruiter(@PathVariable Long userId) {
        adminRecruiterService.approveRecruiter(userId);
    }
}
