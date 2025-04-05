package com.winnguyen1905.Activity.rest.controller;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ClassDto;
import com.winnguyen1905.Activity.model.viewmodel.ClassVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.rest.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {
  private final ClassService classService;

  @PostMapping("/create")
  public ResponseEntity<Void> createClass(@AccountRequest TAccountRequest accountRequest,
      @RequestBody ClassDto classDto) {
    classService.createClass(accountRequest, classDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClassVm> updateClass(@AccountRequest TAccountRequest accountRequest,
      @RequestBody ClassDto classDto,
      @PathVariable Long id) {
    classService.updateClass(accountRequest, classDto, id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteClass(@AccountRequest TAccountRequest accountRequest, @PathVariable Long id) {
    classService.deleteClass(accountRequest, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClassVm> getClassById(@PathVariable Long id) {
    ClassVm classVm = classService.getClassById(id);
    return ResponseEntity.ok(classVm);
  }

  @GetMapping
  public ResponseEntity<PagedResponse<ClassVm>> getAllClasses(Pageable pageable) {
    PagedResponse<ClassVm> classes = classService.getAllClasses(pageable);
    return ResponseEntity.ok(classes);
  }
}
