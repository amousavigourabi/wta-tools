package com.asml.apa.wta.spark.executor;

import com.asml.apa.wta.spark.WtaPlugin;
import com.asml.apa.wta.spark.datasource.IostatDataSource;
import com.asml.apa.wta.spark.driver.WtaDriverPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.TaskFailedReason;
import org.apache.spark.api.plugin.ExecutorPlugin;
import org.apache.spark.api.plugin.PluginContext;
import org.apache.spark.rpc.RpcEndpointRef;
import org.apache.spark.SparkEnv;
import org.apache.spark.scheduler.Task;
import org.apache.spark.scheduler.TaskSchedulerImpl;
import java.util.concurrent.atomic.AtomicReference;



/**
 * Executor component of the plugin.
 *
 * @author Henry Page
 * @since 1.0.0
 */
public class WtaExecutorPlugin implements ExecutorPlugin {

  private PluginContext pluginContext;

  /**
   * This method is called when the plugin is initialized on the executor.
   * Developers are urged not to put inefficient code here as it blocks executor initialization until
   * it is completed.
   *
   * @param pluginContext The PluginContext object that represents the context of the plugin.
   * @param extraConf A map object that contains any extra configuration information. This map
   *                  is directly returned from {@link WtaDriverPlugin#init(SparkContext, PluginContext)}
   * @see WtaPlugin#executorPlugin() where a new instance of the plugin is created. This gets called as soon
   * as it is loaded on to the executor.
   */
  @Override
  public void init(PluginContext pluginContext, Map<String, String> extraConf) {
    this.pluginContext = pluginContext;
  }


  /**
   * Called before task is started.
   * Developers should note that expensive operations should be avoided, since it gets called on every task.
   * Exceptions thrown here are not propagated, meaning a task won't fail if this method throws an exception.
   * <a href="https://spark.apache.org/docs/3.2.1/api/java/org/apache/spark/api/plugin/ExecutorPlugin.html#init-org.apache.spark.api.plugin.PluginContext-java.util.Map-">Refer to the docs</a> for more information.
   */
  @Override
  public void onTaskStart() {//send the iostat metric dto to driver
// Execute the bash command on the executor
    AtomicReference<IostatDataSource> result = new AtomicReference<>(null);
    try {
      IostatDataSource iods = new IostatDataSource();
      iods.setExecutorId(pluginContext.executorID());
      final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      scheduler.scheduleAtFixedRate(() -> {
        try {
          IostatDataSource updatedResult = iods.getAllMetrics();
          result.set(updatedResult);
          // Send the result back to the driver
          try {
            Long taskId = (Long) this.pluginContext.ask(result.get());

          } catch (IOException e) {
            throw new RuntimeException(e);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }, 0, 5, TimeUnit.SECONDS);

      // Initial result before scheduling
      result.set(iods.getAllMetrics());
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets called when a task is successfully completed.
   * Gets called even if {@link #onTaskStart()} threw an exception.
   */
  @Override
  public void onTaskSucceeded() {}

  /**
   * Gets called if a task fails.
   *
   * @param failureReason The reason the task failed, accessible through a string.
   */
  @Override
  public void onTaskFailed(TaskFailedReason failureReason) {}

  /**
   * Gets called just before shutdown. Blocks executor shutdown until it is completed.
   */
  @Override
  public void shutdown() {}
}
