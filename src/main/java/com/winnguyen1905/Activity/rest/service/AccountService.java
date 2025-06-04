package com.winnguyen1905.Activity.rest.service;

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

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.exception.BadRequestException;
import com.winnguyen1905.Activity.model.dto.AdminUpdateAccount;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.dto.UpdateAccountDto;
import com.winnguyen1905.Activity.model.dto.AccountSearchCriteria;
import com.winnguyen1905.Activity.persistance.repository.specification.AccountSpecifications;
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
    return this.accountRepository.findById(accountRequest.id())
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
    Optional<EAccountCredentials> accountOptional = this.accountRepository.findById(accountRequest.id());

    if (accountOptional.isPresent() && accountOptional.get() instanceof EAccountCredentials account) {
      account.setPhone(accountDto.phone());
      account.setEmail(accountDto.email());
      this.accountRepository.save(account);
    } else
      throw new BadRequestException("Not found user");

    return this.getAccount(accountRequest);
  }

  public AccountVm updateAccountByAdmin(TAccountRequest accountRequest, AdminUpdateAccount updateDto) {
    EAccountCredentials account = accountRepository.findById(accountRequest.id())
        .orElseThrow(() -> new BadRequestException("Account not found with id: " + accountRequest.id()));

    // Update basic info
    account.setFullName(updateDto.fullName());
    account.setEmail(updateDto.email());
    account.setPhone(updateDto.phone());
    account.setMajor(updateDto.major());

    // Update role if provided (admin-only field)
    if (updateDto.role() != null) {
      account.setRole(updateDto.role());
    }

    // Update active status if provided (admin-only field)
    if (updateDto.isActive() != null) {
      account.setIsActive(updateDto.isActive());
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
        .fullName(registerRequest.fullName())
        .identifyCode(registerRequest.identifyCode())
        .password(registerRequest.password())
        .isActive(true)
        .email(registerRequest.email())
        .phone(registerRequest.phone())
        .major(registerRequest.major())
        .role(registerRequest.role())
        .build();
    this.accountRepository.save(newUser);
    return this.convertToAccountVm(newUser);
  }

}
