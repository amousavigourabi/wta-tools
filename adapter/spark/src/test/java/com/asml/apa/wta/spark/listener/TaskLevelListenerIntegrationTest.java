package com.asml.apa.wta.spark.listener;

import static org.assertj.core.api.Assertions.assertThat;

import com.asml.apa.wta.spark.BaseSparkJobIntegrationTest;
import org.junit.jupiter.api.Test;

class TaskLevelListenerIntegrationTest extends BaseSparkJobIntegrationTest {

  @Test
  void testGetTaskMetricsHasTasksAfterSparkJobAndYieldsNoErrors() {
    sut.registerTaskListener();
    invokeJob();
    assertThat(sut.getTaskMetrics()).isNotEmpty();
  }

  @Test
  void runningAJobShouldClearTheMapOfEntriesAfterStageIsDoneWorkflowIdShouldBeInitialised() {
    sut.registerTaskListener();
    invokeJob();
    assertThat(((TaskLevelListener) sut.getTaskLevelListener()).getStageIdsToJobs())
        .isEmpty();
    assertThat(sut.getTaskMetrics()).hasSizeGreaterThanOrEqualTo(1);
    assertThat(sut.getTaskMetrics().get(0).getWorkflowId())
        .isInstanceOf(Long.class)
        .isNotNull();
  }
}
