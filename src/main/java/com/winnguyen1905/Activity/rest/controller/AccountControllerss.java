package com.winnguyen1905.Activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class AccountControllerss {

  private final AccountService accountService;

  @GetMapping("")
  public ResponseEntity<PagedResponse<AccountVm>> getAccounts(Pageable pageable) {
    return ResponseEntity.ok().body(this.accountService.getAccount(pageable));
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

}
