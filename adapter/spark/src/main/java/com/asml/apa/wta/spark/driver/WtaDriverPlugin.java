package com.asml.apa.wta.spark.driver;

import com.asml.apa.wta.spark.datasource.SparkDataSource;
import com.asml.apa.wta.spark.datasource.dto.IostatDataSourceDto;
import com.asml.apa.wta.spark.streams.MetricStreamingEngine;
import com.asml.apa.wta.spark.streams.ResourceKey;
import com.asml.apa.wta.spark.streams.ResourceMetricsRecord;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.apache.spark.SparkContext;
import org.apache.spark.api.plugin.DriverPlugin;
import org.apache.spark.api.plugin.PluginContext;

/**
 * Driver component of the plugin.
 *
 * @author Henry Page
 * @since 1.0.0
 */
public class WtaDriverPlugin implements DriverPlugin {

  private SparkContext sparkContext;

  @Getter
  private SparkDataSource sparkDataSource;

  private MetricStreamingEngine mse = new MetricStreamingEngine();

  /**
   * This method is called early in the initialization of the Spark driver.
   * Explicitly, it is called before the Spark driver's task scheduler is initialized. It is blocking.
   *
   * Expensive calls should be postponed or delegated to another thread.
   *
   * @param sparkCtx The current SparkContext.
   * @param pluginCtx Additional plugin-specific about the Spark application where the plugin is running.
   * @return Extra information provided to the executor
   * @author Henry Page
   * @since 1.0.0
   */
  @Override
  public Map<String, String> init(SparkContext sparkCtx, PluginContext pluginCtx) {
    this.sparkContext = sparkCtx;
    this.sparkDataSource = new SparkDataSource(this.sparkContext);
    initListeners();
    this.mse = new MetricStreamingEngine();
    return new HashMap<>();
  }

  /**
   * Gets called just before shutdown. Recommended that no spark functions are used here.
   *
   * @author Henry Page
   * @since 1.0.0
   */
  @Override
  public void shutdown() {}

  @Override
  public Object receive(Object message) {
    if (message instanceof IostatDataSourceDto) {
      onIostatRecieve((IostatDataSourceDto) message);
    }
    return message;
  }

  private void initListeners() {
    this.sparkDataSource.registerTaskListener();
    // register more listeners as needed
  }

  private void onIostatRecieve(IostatDataSourceDto iodsDto) {
    // TODO: Remove print at end (kept now for debugging)
    System.out.println(iodsDto.toString());
    mse.addToResourceStream(new ResourceKey(iodsDto.getExecutorId()), new ResourceMetricsRecord(iodsDto));
  }
}
