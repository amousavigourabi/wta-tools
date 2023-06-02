package com.asml.apa.wta.core.supplier;

import com.asml.apa.wta.core.dto.BaseSupplierDto;
import com.asml.apa.wta.core.dto.IostatDto;
import com.asml.apa.wta.core.dto.OsInfoDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

/**
 * Used to extract resources from dependencies. Should be instantiated once per executor/node.
 *
 * @author Henry Page
 * @since 1.0.0
 */
public abstract class SupplierExtractionEngine<T extends BaseSupplierDto> {

  private final OperatingSystemSupplier operatingSystemSupplier;

  private final IostatSupplier iostatSupplier;

  @Getter
  private final Collection<T> buffer = new ArrayList<>();

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  /**
   * Constructor for the resource extraction engine.
   * Suppliers should be injected here.
   *
   * @author Henry Page
   * @since 1.0.0
   */
  public SupplierExtractionEngine() {
    this.operatingSystemSupplier = new OperatingSystemSupplier();
    this.iostatSupplier = new IostatSupplier();
  }

  /**
   * Ping the suppliers and add the results to the buffer.
   * Developers are encouraged to override this to add/remove additional information.
   *
   * @return A completablefuture that completes when the result has been resolved
   * @author Henry Page
   * @since 1.0.0
   */
  public CompletableFuture<Void> ping() {
    CompletableFuture<OsInfoDto> osInfoDtoCompletableFuture = this.operatingSystemSupplier.getSnapshot();
    CompletableFuture<IostatDto> iostatDtoCompletableFuture = this.iostatSupplier.getSnapshot();

    return CompletableFuture.allOf(osInfoDtoCompletableFuture, iostatDtoCompletableFuture)
        .thenRunAsync(() -> {
          LocalDateTime timestamp = LocalDateTime.now();
          OsInfoDto osInfoDto = osInfoDtoCompletableFuture.join();
          IostatDto iostatDto = iostatDtoCompletableFuture.join();

          buffer.add(transform(new BaseSupplierDto(timestamp, osInfoDto, iostatDto)));
        });
  }

  /**
   * Starts pinging the suppliers at a fixed rate.
   *
   * @param resourcePingInterval How often to ping the suppliers, in milliseconds
   * @author Henry Page
   * @since 1.0.0
   */
  public void startPinging(int resourcePingInterval) {
    scheduler.scheduleAtFixedRate(this::ping, 0, resourcePingInterval, TimeUnit.MILLISECONDS);
  }

  /**
   * Stops pinging the suppliers.
   *
   * @author Henry Page
   * @since 1.0.0
   */
  public void stopPinging() {
    scheduler.shutdown();
  }

  /**
   * Transform the resource metrics record.
   * This method can be overridden by extending classes to add additional information.
   *
   * @param record The 'bare-bones' information that needs to be augmented
   * @return The transformed resource metrics record, with additional information
   * @author Henry Page
   * @since 1.0.0
   */
  public abstract T transform(BaseSupplierDto record);

  /**
   * Get and clear the buffer.
   *
   * @return The buffer contents as a list
   * @author Henry Page
   * @since 1.0.0
   */
  public List<T> getAndClear() {
    List<T> result = new ArrayList<>(buffer);
    buffer.clear();
    return result;
  }
}
