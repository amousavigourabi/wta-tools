package com.asml.apa.wta.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing filesystem metrics accessible from the JVM.
 *
 * @author Henry Page
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JvmFileDto implements SupplierDto {

  private static final long serialVersionUID = 87243L;

  private long totalSpace;

  private long freeSpace;

  private long usableSpace;
}
