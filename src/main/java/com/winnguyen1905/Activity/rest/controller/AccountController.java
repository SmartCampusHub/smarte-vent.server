package com.winnguyen1905.Activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.MajorType;
import com.winnguyen1905.Activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.dto.UpdateAccountDto;
import com.winnguyen1905.Activity.model.viewmodel.AccountVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.rest.service.AccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/create")
  public ResponseEntity<AccountVm> createAccount(@AccountRequest TAccountRequest accountRequest,
      @RequestBody RegisterRequest registerRequest) {
    return ResponseEntity.ok().body(this.accountService.createAccount(accountRequest, registerRequest));
  }

  @PostMapping("/update")
  public ResponseEntity<AccountVm> updateAccount(@AccountRequest TAccountRequest accountRequest,
      @RequestBody UpdateAccountDto accountDto) {
    return ResponseEntity.ok().body(this.accountService.updateAccount(accountRequest, accountDto));
  }

  @GetMapping("")
  public ResponseEntity<PagedResponse<AccountVm>> searchAccounts(
      @RequestParam(required = false) String fullName,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String identifyCode,
      @RequestParam(required = false) AccountRole role,
      @RequestParam(required = false) MajorType major,
      @RequestParam(required = false) Boolean isActive,
      Pageable pageable) {

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
  public ResponseEntity<Void> changeAccountStatus(@PathVariable Long id) {
    this.accountService.changeStatus(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/delete")
  public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
    this.accountService.deleteAccount(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/my-account")
  public ResponseEntity<AccountVm> getAccount(@AccountRequest TAccountRequest accountRequest) {
    return ResponseEntity.ok().body(this.accountService.getAccount(accountRequest));
  }

}
