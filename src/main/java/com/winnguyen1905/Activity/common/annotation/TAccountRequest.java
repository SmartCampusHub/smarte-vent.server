package com.winnguyen1905.activity.common.annotation;

import java.io.Serializable;
import java.util.UUID;
import com.winnguyen1905.activity.common.constant.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TAccountRequest implements Serializable {
  private Long id;
  private String username;
  private AccountRole role;
  private UUID socketClientId;
}
