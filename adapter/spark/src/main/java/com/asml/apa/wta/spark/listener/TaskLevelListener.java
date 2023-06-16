package com.asml.apa.wta.spark.listener;

import com.asml.apa.wta.core.config.RuntimeConfig;
import com.asml.apa.wta.core.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.apache.spark.SparkContext;
import org.apache.spark.executor.ShuffleWriteMetrics;
import org.apache.spark.executor.TaskMetrics;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.TaskInfo;

/**
 * This class is a task-level listener for the Spark data source.
 *
 * @author Pil Kyu Cho
 * @author Henry Page
 * @author Tianchen Qu
 * @since 1.0.0
 */
@Getter
public class TaskLevelListener extends TaskStageBaseListener {

  private final Map<Integer, List<Task>> stageToTasks = new ConcurrentHashMap<>();

  private final Map<Long, Integer> taskToStage = new ConcurrentHashMap<>();

  /**
   * Constructor for the task-level listener.
   *
   * @param sparkContext       The current spark context
   * @param config             Additional config specified by the user for the plugin
   * @author Henry Page
   * @since 1.0.0
   */
  public TaskLevelListener(SparkContext sparkContext, RuntimeConfig config) {
    super(sparkContext, config);
  }

  /**
   * This method is called every time a task ends, task-level metrics should be collected here, and added.
   *
   * @param taskEnd   SparkListenerTaskEnd The object corresponding to information on task end
   * @author Henry Page
   * @author Tianchen Qu
   * @since 1.0.0
   */
  @Override
  public void onTaskEnd(SparkListenerTaskEnd taskEnd) {
    final TaskInfo curTaskInfo = taskEnd.taskInfo();
    final TaskMetrics curTaskMetrics = taskEnd.taskMetrics();
    final long taskId = curTaskInfo.taskId() + 1;
    final String type = taskEnd.taskType();
    final long submitTime = curTaskInfo.launchTime();
    final long runTime = curTaskMetrics.executorRunTime();
    final int userId = sparkContext.sparkUser().hashCode();
    final int stageId = taskEnd.stageId();
    final long workflowId = stageIdsToJobs.get(taskEnd.stageId() + 1);

    final long[] parents = new long[0];
    final long[] children = new long[0];
    taskToStage.put(taskId, stageId);
    ShuffleWriteMetrics temp = curTaskMetrics.shuffleWriteMetrics();
    final double diskSpaceRequested = (double) curTaskMetrics.diskBytesSpilled() + temp.bytesWritten();
    // final double memoryRequested = curTaskMetrics.peakExecutionMemory();
    /**
     *  peakExecutionMemory is the peak memory used by internal data structures created during shuffles, aggregations
     *  and joins.
     *  The value of this accumulator should be approximately the sum of the peak sizes across all such data structures
     *  created in this task.
     *  It is thus only an upper bound of the actual peak memory for the task.
     *  For SQL jobs, this only tracks all unsafe operators and ExternalSort
     */
    final double memoryRequested = -1.0;
    final String resourceType = "N/A";
    final double resourceAmountRequested = -1.0;
    final long diskIoTime =
        curTaskMetrics.executorDeserializeTime() + curTaskMetrics.resultSerializationTime(); // unsure
    final long resourceUsed = Math.abs(curTaskInfo.executorId().hashCode());

    // unknown
    final int submissionSite = -1;
    final int groupId = -1;
    final String nfrs = "";
    final String params = "";
    final long networkIoTime = -1L;

    final double energyConsumption = -1L;
    final long waitTime = -1L;

    // TODO(#61): CALL EXTERNAL DEPENDENCIES
    Task task = Task.builder()
        .id(taskId)
        .type(type)
        .submissionSite(submissionSite)
        .tsSubmit(submitTime)
        .runtime(runTime)
        .resourceType(resourceType)
        .resourceAmountRequested(resourceAmountRequested)
        .parents(parents)
        .children(children)
        .userId(userId)
        .groupId(groupId)
        .nfrs(nfrs)
        .workflowId(workflowId)
        .waitTime(waitTime)
        .params(params)
        .memoryRequested(memoryRequested)
        .networkIoTime(networkIoTime)
        .diskIoTime(diskIoTime)
        .diskSpaceRequested(diskSpaceRequested)
        .energyConsumption(energyConsumption)
        .resourceUsed(resourceUsed)
        .build();

    final List<Task> tasks = stageToTasks.get(stageId);
    if (tasks == null) {
      List<Task> newTasks = new ArrayList<>();
      newTasks.add(task);
      stageToTasks.put(stageId, newTasks);
    } else {
      tasks.add(task);
    }
    this.getProcessedObjects().add(task);
  }
}
