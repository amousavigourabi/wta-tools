package com.asml.apa.wta.core.Config;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuntimeConfig {

  private String author;

  private String domain;

  @Builder.Default
  private String description = "";
}
