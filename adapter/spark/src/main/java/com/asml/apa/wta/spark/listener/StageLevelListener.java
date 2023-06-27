package com.asml.apa.wta.spark.listener;

import com.asml.apa.wta.core.config.RuntimeConfig;
import com.asml.apa.wta.core.model.Task;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.apache.spark.SparkContext;
import org.apache.spark.executor.TaskMetrics;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.scheduler.SparkListenerStageCompleted;
import org.apache.spark.scheduler.StageInfo;
import scala.collection.JavaConverters;

/**
 * This class is a stage-level listener for the Spark data source.
 *
 * @author Tianchen Qu
 * @author Lohithsai Yadala Chanchu
 * @since 1.0.0
 */
@Getter
public class StageLevelListener extends TaskStageBaseListener {

  private final Map<Long, Long[]> stageToParents = new ConcurrentHashMap<>();

  private final Map<Long, List<Long>> parentStageToChildrenStages = new ConcurrentHashMap<>();

  private final Map<Long, Integer> stageToResource = new ConcurrentHashMap<>();

  /**
   * Constructor for the stage-level listener from Spark datasource class.
   *
   * @param sparkContext       The current spark context
   * @param config             Additional config specified by the user for the plugin
   * @author Pil Kyu Cho
   * @since 1.0.0
   */
  public StageLevelListener(SparkContext sparkContext, RuntimeConfig config) {
    super(sparkContext, config);
  }

  /**
   * This method will store the stage hierarchy information from the callback, depending on the isStageLevel config.
   * Only the parents of the current stage is determined here. The children are determined on application end in
   * ApplicationLevelListener.
   * @param stageId           Current stage id
   * @param task              Task object of current stage metrics
   * @param curStageInfo      Current stage info
   * @author Pil Kyu Cho
   * @since 1.0.0
   */
  public void fillInParentChildMaps(long stageId, Task task, StageInfo curStageInfo) {
    final long[] parentStageIds = JavaConverters.seqAsJavaList(
            curStageInfo.parentIds().toList())
        .stream()
        .mapToLong(parentId -> (Integer) parentId + 1)
        .toArray();
    task.setParents(parentStageIds);
    if (!getConfig().isStageLevel()) {
      stageToParents.put(stageId, Arrays.stream(parentStageIds).boxed().toArray(Long[]::new));
    }

    // Add current task as child to its parent stages
    for (Long parentStageId : parentStageIds) {
      List<Long> childrenStages = parentStageToChildrenStages.get(parentStageId);
      if (childrenStages == null) {
        childrenStages = new ArrayList<>();
        childrenStages.add(stageId);
        parentStageToChildrenStages.put(parentStageId, childrenStages);
      } else {
        childrenStages.add(stageId);
      }
    }
  }

  /**
   * This method is called every time a stage is completed. Stage-level metrics are collected, aggregated,
   * and added here.
   *
   * @param stageCompleted   SparkListenerStageCompleted The object corresponding to information on stage completion
   * @author Tianchen Qu
   * @author Lohithsai Yadala Chanchu
   * @author Pil Kyu Cho
   * @since 1.0.0
   */
  @Override
  public void onStageCompleted(SparkListenerStageCompleted stageCompleted) {
    final StageInfo curStageInfo = stageCompleted.stageInfo();
    final TaskMetrics curStageMetrics = curStageInfo.taskMetrics();
    final long stageId = curStageInfo.stageId() + 1;
    stageToResource.put(stageId, curStageInfo.resourceProfileId());

    final long tsSubmit = curStageInfo.submissionTime().getOrElse(() -> -1L);
    final long runtime = curStageInfo.taskMetrics().executorRunTime();
    final int userId = Math.abs(getSparkContext().sparkUser().hashCode());
    final long workflowId = getStageToJob().get(stageId);
    final double diskSpaceRequested = (double) curStageMetrics.diskBytesSpilled()
        + curStageMetrics.shuffleWriteMetrics().bytesWritten();

    Task task = Task.builder()
        .id(stageId)
        .tsSubmit(tsSubmit)
        .runtime(runtime)
        .userId(userId)
        .workflowId(workflowId)
        .diskSpaceRequested(diskSpaceRequested)
        .build();

    fillInParentChildMaps(stageId, task, curStageInfo);

    addTaskToWorkflow(workflowId, task);

    if (getConfig().isStageLevel()) {
      getThreadPool().execute(() -> addProcessedObject(task));
    }
  }

  /**
   * Sets up the stage children, and it shall be called on job end in
   * {@link JobLevelListener#onJobEnd(SparkListenerJobEnd)}. It only sets the Stages which are
   * affiliated to the passed jobId.
   *
   * @param jobId            Spark Job id to filter Stages by
   * @author Tianchen Qu
   * @author Pil Kyu Cho
   * @since 1.0.0
   */
  public void setStages(long jobId) {
    getWorkflowsToTasks().onKey(jobId).forEach(stage -> stage.setChildren(
        this.getParentStageToChildrenStages().getOrDefault(stage.getId(), new ArrayList<>()).stream()
            .mapToLong(Long::longValue)
            .toArray()));
  }
}
