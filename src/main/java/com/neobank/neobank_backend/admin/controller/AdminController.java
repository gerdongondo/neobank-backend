package com.neobank.neobank_backend.admin.controller;

import com.neobank.neobank_backend.admin.dto.AdminStatsResponse;
import com.neobank.neobank_backend.admin.dto.TransactionAdminResponse;
import com.neobank.neobank_backend.admin.dto.UserAdminResponse;
import com.neobank.neobank_backend.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserAdminResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserAdminResponse>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchUsers(keyword));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        adminService.blockAccount(id);
        return ResponseEntity.ok(Map.of(
                "message", "Compte bloqué avec succès",
                "userId", id
        ));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        adminService.unblockAccount(id);
        return ResponseEntity.ok(Map.of(
                "message", "Compte débloqué avec succès",
                "userId", id
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionAdminResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAllTransactions(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}