package com.winnguyen1905.activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import com.winnguyen1905.activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.activity.model.dto.RegisterRequest;
import com.winnguyen1905.activity.model.dto.UpdateAccountDto;
import com.winnguyen1905.activity.model.viewmodel.AccountVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.rest.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
@Tag(name = "Account Management", description = "Operations related to account management")
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/create")
  @Operation(summary = "Create a new account", description = "Creates a new user account with the provided details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Account created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "409", description = "Account already exists")
  })
  public ResponseEntity<AccountVm> createAccount(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Account registration details", required = true) @RequestBody RegisterRequest registerRequest) {
    this.accountService.createAccount(accountRequest, registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/update")
  @Operation(summary = "Update account", description = "Updates an existing account with the provided details")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Account updated successfully", 
                 content = @Content(schema = @Schema(implementation = AccountVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "404", description = "Account not found")
  })
  public ResponseEntity<AccountVm> updateAccount(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Account update details", required = true) @RequestBody UpdateAccountDto accountDto) {
    return ResponseEntity.ok().body(this.accountService.updateAccount(accountRequest, accountDto));
  }

  @GetMapping("")
  @Operation(summary = "Search accounts", description = "Search for accounts based on various criteria with pagination")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search completed successfully", 
                 content = @Content(schema = @Schema(implementation = PagedResponse.class)))
  })
  public ResponseEntity<PagedResponse<AccountVm>> searchAccounts(
      @Parameter(description = "Full name of the account holder") @RequestParam(required = false) String fullName,
      @Parameter(description = "Email address") @RequestParam(required = false) String email,
      @Parameter(description = "Phone number") @RequestParam(required = false) String phone,
      @Parameter(description = "Identification code") @RequestParam(required = false) String identifyCode,
      @Parameter(description = "Account role") @RequestParam(required = false) AccountRole role,
      @Parameter(description = "Major type") @RequestParam(required = false) MajorType major,
      @Parameter(description = "Account active status") @RequestParam(required = false) Boolean isActive,
      @Parameter(description = "Pagination parameters") Pageable pageable) {

    AccountSearchCriteria criteria = AccountSearchCriteria.builder()
        .fullName(fullName)
        .email(email)
        .phone(phone)
        .identifyCode(identifyCode)
        .role(role)
        .major(major)
        .isActive(isActive)
        .build();

    return ResponseEntity.ok().body(this.accountService.searchAccounts(criteria, pageable));
  }

  @PostMapping("/{id}/change-status")
  @Operation(summary = "Change account status", description = "Activate or deactivate an account by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Account status changed successfully"),
    @ApiResponse(responseCode = "404", description = "Account not found")
  })
  public ResponseEntity<Void> changeAccountStatus(
      @Parameter(description = "ID of the account to change status", required = true) @PathVariable Long id) {
    this.accountService.changeStatus(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/delete")
  @Operation(summary = "Delete account", description = "Delete an account by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Account not found")
  })
  public ResponseEntity<Void> deleteAccount(
      @Parameter(description = "ID of the account to delete", required = true) @PathVariable Long id) {
    this.accountService.deleteAccount(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/my-account")
  @Operation(summary = "Get current account", description = "Get details of the currently authenticated account")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Account details retrieved successfully", 
                 content = @Content(schema = @Schema(implementation = AccountVm.class))),
    @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  public ResponseEntity<AccountVm> getAccount(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
    return ResponseEntity.ok().body(this.accountService.getAccount(accountRequest));
  }

}
