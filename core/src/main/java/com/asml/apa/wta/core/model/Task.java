package com.asml.apa.wta.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

/**
 * Task class corresponding to WTA format.
 *
 * @author  Lohithsai Yadala Chanchu
 * @since 1.0.0
 */
@Data
@Builder
public class Task implements BaseTraceObject {
  private static final long serialVersionUID = -1372345471722101373L;

  @Setter
  private static String schemaVersion;

  private final long id;

  private final String type;

  private final long submitType;

  private final int submission_site;

  private final long runtime;

  private final String resourceType;

  private final double resourceAmountRequested;

  private final long[] parents;

  private final long[] children;

  private final int userId;

  private final int groupId;

  private final String nfrs;

  private final long workflowId;

  private final long waitTime;

  private final String params;

  private final double memoryRequested;

  private final long networkIOTime;

  private final long diskIOTime;

  private final double diskSpaceRequested;

  private final long energyConsumption;

  private final long resourceUsed;
}
