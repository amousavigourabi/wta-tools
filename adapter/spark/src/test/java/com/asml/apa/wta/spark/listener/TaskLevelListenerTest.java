package com.asml.apa.wta.spark.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.asml.apa.wta.core.model.Task;
import java.util.Properties;
import org.apache.spark.executor.ExecutorMetrics;
import org.apache.spark.executor.TaskMetrics;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.StageInfo;
import org.apache.spark.scheduler.TaskInfo;
import org.apache.spark.scheduler.TaskLocality;
import org.apache.spark.scheduler.TaskLocation;
import org.apache.spark.storage.RDDInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scala.collection.immutable.Seq;
import scala.collection.mutable.ListBuffer;

class TaskLevelListenerTest extends BaseLevelListenerTest {

  TaskInfo testTaskInfo;

  StageInfo testStageInfo;

  SparkListenerTaskEnd taskEndEvent;

  @BeforeEach
  void setup() {

    testTaskInfo = new TaskInfo(1, 0, 1, 50L, "testExecutor", "local", TaskLocality.NODE_LOCAL(), false);

    TaskMetrics mockedMetrics = mock(TaskMetrics.class);
    when(mockedMetrics.executorRunTime()).thenReturn(100L);

    testStageInfo = new StageInfo(
        1,
        1,
        "testStage",
        1,
        new ListBuffer<RDDInfo>().toList(),
        new ListBuffer<>().toList(),
        "details",
        new TaskMetrics(),
        new ListBuffer<Seq<TaskLocation>>().toList(),
        null,
        3);
    taskEndEvent = new SparkListenerTaskEnd(
        1, 1, "testtaskType", null, testTaskInfo, new ExecutorMetrics(), mockedMetrics);
  }

  @Test
  void testTaskEndMetricExtraction() {
    ListBuffer<StageInfo> stageBuffer = new ListBuffer<>();
    stageBuffer.addOne(testStageInfo);

    fakeTaskListener.onJobStart(new SparkListenerJobStart(1, 2L, stageBuffer.toList(), new Properties()));
    fakeTaskListener.onTaskEnd(taskEndEvent);
    assertEquals(1, fakeTaskListener.getProcessedObjects().size());
    Task curTask = fakeTaskListener.getProcessedObjects().get(0);
    assertEquals(1, curTask.getId());
    assertEquals("testtaskType", curTask.getType());
    assertEquals(50L, curTask.getSubmitTime());
    assertEquals(100L, curTask.getRuntime());
    assertEquals(1L, curTask.getWorkflowId());
    assertEquals("testUser".hashCode(), curTask.getUserId());
    assertEquals(-1, curTask.getSubmissionSite());
    assertEquals("N/A", curTask.getResourceType());
    assertEquals(-1.0, curTask.getResourceAmountRequested());
    assertEquals(-1.0, curTask.getMemoryRequested());
    assertEquals(-1.0, curTask.getDiskSpaceRequested());
    assertEquals(-1L, curTask.getEnergyConsumption());
    assertEquals(-1L, curTask.getNetworkIoTime());
    assertEquals(-1L, curTask.getDiskIoTime());
    assertEquals(-1, curTask.getGroupId());
    assertEquals("", curTask.getNfrs());
    assertEquals("", curTask.getParams());
    assertEquals(0, curTask.getParents().length);
    assertEquals(0, curTask.getChildren().length);
  }
}
