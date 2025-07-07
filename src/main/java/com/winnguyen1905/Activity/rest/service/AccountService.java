package com.winnguyen1905.activity.rest.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.exception.BadRequestException; 
import com.winnguyen1905.activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.activity.model.dto.AdminUpdateAccount;
import com.winnguyen1905.activity.persistance.repository.specification.AccountSpecifications;
import com.winnguyen1905.activity.model.dto.RegisterRequest;
import com.winnguyen1905.activity.model.dto.UpdateAccountDto;
import com.winnguyen1905.activity.model.viewmodel.AccountVm;
import com.winnguyen1905.activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.utils.JwtUtils;

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

  public PagedResponse<AccountVm> searchAccounts(AccountSearchCriteria criteria, Pageable pageable) {
    if (criteria == null) {
      criteria = new AccountSearchCriteria();
    }
    Specification<EAccountCredentials> spec = AccountSpecifications.withCriteria(criteria);

    Page<EAccountCredentials> accountPage = this.accountRepository.findAll(spec, pageable);

    List<AccountVm> accounts = accountPage.getContent().stream()
        .map(this::convertToAccountVm)
        .collect(Collectors.toList());

    return PagedResponse.<AccountVm>builder()
        .results(accounts)
        .page(accountPage.getNumber())
        .size(accountPage.getSize())
        .totalElements(accountPage.getTotalElements())
        .totalPages(accountPage.getTotalPages())
        .build();
  }

  public PagedResponse<AccountVm> getAccount(Pageable pageable) {
    Page<EAccountCredentials> accountPage = this.accountRepository.findAll(pageable);

    List<AccountVm> accounts = accountPage.getContent().stream()
        .map(this::convertToAccountVm)
        .collect(Collectors.toList());

    return PagedResponse.<AccountVm>builder()
        .results(accounts)
        .page(accountPage.getNumber())
        .size(accountPage.getSize())
        .totalElements(accountPage.getTotalElements())
        .totalPages(accountPage.getTotalPages())
        .build();
  }

  private AccountVm convertToAccountVm(EAccountCredentials account) {
    return AccountVm.builder()
        .identifyCode(account.getIdentifyCode())
        .role(account.getRole())
        .id(account.getId())
        .name(account.getFullName())
        .email(account.getEmail())
        .isActive(account.getIsActive())
        .phone(account.getPhone())
        .major(account.getMajor())
        .build();
  }

  public void changeStatus(Long id) {
    Optional<EAccountCredentials> accountOptional = this.accountRepository.findById(id);

    if (accountOptional.isPresent() && accountOptional.get() instanceof EAccountCredentials account) {
      account.setIsActive(!account.getIsActive());
      this.accountRepository.save(account);
    } else
      throw new BadRequestException("Not found user");
  }

  public void deleteAccount(Long id) {
    this.accountRepository.deleteById(id);
  }

  public AccountVm getAccount(TAccountRequest accountRequest) {
    return this.accountRepository.findById(accountRequest.getId())
        .map(account -> {
          return AccountVm.builder()
              .identifyCode(account.getIdentifyCode())
              .role(account.getRole())
              .id(account.getId())
              .name(account.getFullName())
              .email(account.getEmail())
              .isActive(account.getIsActive())
              .phone(account.getPhone())
              .major(account.getMajor())
              .build();
        })
        .orElseThrow(() -> new BadRequestException("Not found user"));
  }

  public AccountVm updateAccount(TAccountRequest accountRequest, UpdateAccountDto accountDto) {
    Optional<EAccountCredentials> accountOptional = this.accountRepository.findById(accountRequest.getId());

    if (accountOptional.isPresent() && accountOptional.get() instanceof EAccountCredentials account) {
      account.setPhone(accountDto.getPhone());
      account.setEmail(accountDto.getEmail());
      this.accountRepository.save(account);
    } else
      throw new BadRequestException("Not found user");

    return this.getAccount(accountRequest);
  }

  public AccountVm updateAccountByAdmin(TAccountRequest accountRequest, AdminUpdateAccount updateDto) {
    EAccountCredentials account = accountRepository.findById(accountRequest.getId())
        .orElseThrow(() -> new BadRequestException("Account not found with id: " + accountRequest.getId()));

    // Update basic info
    account.setFullName(updateDto.getFullName());
    account.setEmail(updateDto.getEmail());
    account.setPhone(updateDto.getPhone());
    account.setMajor(updateDto.getMajor());

    // Update role if provided (admin-only field)
    if (updateDto.getRole() != null) {
      account.setRole(updateDto.getRole());
    }

    // Update active status if provided (admin-only field)
    if (updateDto.getIsActive() != null) {
      account.setIsActive(updateDto.getIsActive());
    }

    EAccountCredentials savedAccount = accountRepository.save(account);

    return AccountVm.builder()
        .id(savedAccount.getId())
        .name(savedAccount.getFullName())
        .email(savedAccount.getEmail())
        .phone(savedAccount.getPhone())
        .major(savedAccount.getMajor())
        .role(savedAccount.getRole())
        .identifyCode(savedAccount.getIdentifyCode())
        .isActive(savedAccount.getIsActive())
        .build();
  }

  public AccountVm createAccount(TAccountRequest accountRequest, RegisterRequest registerRequest) {
    EAccountCredentials newUser = EAccountCredentials.builder()
        .fullName(registerRequest.getFullName())
        .identifyCode(registerRequest.getIdentifyCode())
        .password(registerRequest.getPassword())
        .isActive(true)
        .email(registerRequest.getEmail())
        .phone(registerRequest.getPhone())
        .major(registerRequest.getMajor())
        .role(registerRequest.getRole())
        .build();
    this.accountRepository.save(newUser);
    return this.convertToAccountVm(newUser);
  }

}
