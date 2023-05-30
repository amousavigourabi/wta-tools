package com.asml.apa.wta.spark.driver;

import com.asml.apa.wta.core.config.RuntimeConfig;
import com.asml.apa.wta.core.model.Task;
import com.asml.apa.wta.core.model.Workflow;
import com.asml.apa.wta.core.model.Workload;
import com.asml.apa.wta.core.utils.ParquetWriterUtils;
import com.asml.apa.wta.core.utils.WtaUtils;
import com.asml.apa.wta.spark.datasource.SparkDataSource;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkContext;
import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.PluginContext;

/**
 * Driver component of the plugin. Only one instance of this class is initialized per Spark session.
 *
 * @author Pil Kyu Cho
 * @author Lohithsai Yadala Chanchu
 * @author Atour Mousavi Gourabi
 * @author Henry Page
 * @since 1.0.0
 */
@Slf4j
public class WtaDriverPlugin implements DriverPlugin {

  @Getter
  private SparkDataSource sparkDataSource;

  private ParquetWriterUtils parquetUtil;

  private boolean noError = true;

  /**
   * This method is called early in the initialization of the Spark driver.
   * Explicitly, it is called before the Spark driver's task scheduler is initialized. It is blocking.
   * Expensive calls should be postponed or delegated to another thread. If an error occurs while
   * initializing the plugin, the plugin should call {@link #shutdown()} with noError set to false.
   *
   * @param sparkCtx The current SparkContext.
   * @param pluginCtx Additional plugin-specific about the Spark application where the plugin is running.
   * @return Extra information provided to the executor
   * @author Pil Kyu Cho
   * @author Henry Page
   * @since 1.0.0
   */
  @Override
  public Map<String, String> init(SparkContext sparkCtx, PluginContext pluginCtx) {
    try{
      RuntimeConfig runtimeConfig = WtaUtils.readConfig(System.getProperty("configFile"));
      sparkDataSource = new SparkDataSource(sparkCtx, runtimeConfig);
      parquetUtil = new ParquetWriterUtils(new File(runtimeConfig.getOutputPath()), "schema-1.0");
      parquetUtil.deletePreExistingFiles();
      initListeners();
    } catch (Exception e) {
      noError = false;
      shutdown();
    }
    return new HashMap<>();
  }

  /**
   * Receives messages from the executors.
   *
   * @param message the message that was sent by the executors, to be serializable
   * @return a response to the executor, if no response is expected the result is ignored
   * @author Atour Mousavi Gourabi
   */
  @Override
  public Object receive(Object message) {
    return null;
  }

  /**
   * Gets called just before shutdown. If no prior error occurred, it collects all the
   * tasks, workflows, and workloads from the Spark job and writes them to a parquet file.
   * Otherwise, logs the error and just shuts down.
   * Recommended that no spark functions are used here.
   *
   * @author Pil Kyu Cho
   * @author Henry Page
   * @since 1.0.0
   */
  @Override
  public void shutdown() {
    if (noError) {
      try {
        List<Task> tasks = sparkDataSource.getTaskLevelListener().getProcessedObjects();
        List<Workflow> workFlow = sparkDataSource.getJobLevelListener().getProcessedObjects();
        Workload workLoad =
                sparkDataSource.getApplicationLevelListener().getProcessedObjects().get(0);
        parquetUtil.getTasks().addAll(tasks);
        parquetUtil.getWorkflows().addAll(workFlow);
        parquetUtil.readWorkload(workLoad);
        parquetUtil.writeToFile(
                "resource",
                "task",
                "workflow",
                "generic_information"
        );
        log.info("shutting down plugin without error");
      } catch(Exception e) {
        log.error("Error while writing to Parquet file");
      }
    } else {
      log.error("Error initializing WTA plugin. Shutting down gracefully");
    }
  }

  /**
   * Initializes the listeners.
   *
   * @author Pil Kyu Cho
   * @author Lohithsai Yadala Chanchu
   * @since 1.0.0
   */
  private void initListeners() {
    this.sparkDataSource.registerTaskListener();
    this.sparkDataSource.registerJobListener();
    this.sparkDataSource.registerApplicationListener();
  }
}
