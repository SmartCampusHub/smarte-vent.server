package com.winnguyen1905.activity.rest.controller;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.AdminUpdateAccount;
import com.winnguyen1905.activity.model.viewmodel.AccountVm;
import com.winnguyen1905.activity.rest.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("admin")
public class AdminController {

  private final AccountService accountService;

  @PostMapping("/accounts/update")
  public ResponseEntity<AccountVm> updateAccount(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody AdminUpdateAccount updateDto) {
    AccountVm updatedAccount = accountService.updateAccountByAdmin(accountRequest, updateDto);
    return ResponseEntity.ok(updatedAccount);
  }
}
