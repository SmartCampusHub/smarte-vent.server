package com.winnguyen1905.Activity.rest.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.viewmodel.AccountVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final JwtUtils jwtUtils;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public PagedResponse<AccountVm> getAccount(Pageable pageable) {
    Page<EAccountCredentials> accountPage = this.accountRepository.findAll(pageable);

    List<AccountVm> accounts = accountPage.getContent().stream().map(account -> {
      return AccountVm.builder()
          .studentCode(account.getStudentCode()).role(account.getRole()).id(account.getId())
          .name(account.getFullName()).email(account.getEmail()).isActive(account.getIsActive())
          .phone(account.getPhone()).build();
    }).toList();

    return PagedResponse.<AccountVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(pageable.getPageNumber())
        .size(accountPage.getSize())
        .results(accounts)
        .totalElements((int) accountPage.getTotalElements())
        .totalPages(accountPage.getTotalPages())
        .build();
  }

  
}
