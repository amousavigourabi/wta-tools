package com.asml.apa.wta.core.supplier;

import com.asml.apa.wta.core.dto.DstatDto;
import com.asml.apa.wta.core.utils.ShellUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * DstatDataSource class.
 *
 * @author Lohithsai Yadala Chanchu
 * @since 1.0.0
 */
@Slf4j
public class DstatSupplier implements InformationSupplier<DstatDto> {
  private final ShellUtils shellUtils;
  private final boolean isDstatAvailable;

  public DstatSupplier(ShellUtils shellUtils) {
    this.shellUtils = shellUtils;
    this.isDstatAvailable = isAvailable();
  }

  /**
   * Uses the Dstat dependency to get io metrics.
   *
   * @return DstatDataSourceDto object that will be sent to the driver (with the necessary information filled out)
   * @author Lohithsai Yadala Chanchu
   * @since 1.0.0
   */
  @Override
  public CompletableFuture<Optional<DstatDto>> getSnapshot() {
    if (!isDstatAvailable) {
      return notAvailableResult();
    }

    CompletableFuture<String> allMetrics = shellUtils.executeCommand("dstat -cdngy 1 1", false);

    return allMetrics.thenApply(result -> {
      if (result != null) {
        try {
          List<Long> metrics = extractNumbers(result);
          if (metrics.size() == 13) {
            return Optional.of(DstatDto.builder()
                .totalUsageUsr(metrics.get(0))
                .totalUsageSys(metrics.get(1))
                .totalUsageIdl(metrics.get(2))
                .totalUsageWai(metrics.get(3))
                .totalUsageStl(metrics.get(4))
                .dskRead(metrics.get(5))
                .dskWrite(metrics.get(6))
                .netRecv(metrics.get(7))
                .netSend(metrics.get(8))
                .pagingIn(metrics.get(9))
                .pagingOut(metrics.get(10))
                .systemInt(metrics.get(11))
                .systemCsw(metrics.get(12))
                .build());
          }
        } catch (NullPointerException e) {
          log.error("A null pointer exception when gather metrics from dstat");
        } catch (IndexOutOfBoundsException e) {
          log.error("A different number of dstat metrics were found than expected");
        } catch (NumberFormatException e) {
          log.error("Something went wrong while parsing dstat terminal output");
        } catch (Exception e) {
          log.error("Something went wrong while handling dstat metrics");
        }
      }
      return Optional.empty();
    });
  }

  /**
   * Parse dstat terminal output.
   *
   * @return List of the parsed numbers from the dstat terminal output
   * @author Lohithsai Yadala Chanchu
   * @since 1.0.0
   */
  private static List<Long> extractNumbers(String input) {

    List<Long> numbers = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\b(\\d+)(k|B|G|M)?\\b");
    Matcher matcher = pattern.matcher(input);

    while (matcher.find()) {
      String match = matcher.group(1);
      String suffix = matcher.group(2);

      long number = Integer.parseInt(match);
      if (suffix != null) {
        if (suffix.equals("k")) {
          number *= 1000;
        } else if (suffix.equals("M")) {
          number *= 1000000;
        } else if (suffix.equals("G")) {
          number *= 1000000000;
        }
      }
      numbers.add(number);
    }
    return numbers;
  }

  /**
   * Checks if the dstat datasource is available.
   *
   * @return A boolean that represents if the dstat datasource is available
   * @author Lohithsai Yadala Chanchu
   * @since 1.0.0
   */
  @Override
  public boolean isAvailable() {
    if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
      log.info("The dstat dependency is not available.");
      return false;
    }
    try {
      if (shellUtils.executeCommand("dstat -cdngy 1 1", true).get() != null) {
        return true;
      }
      return false;
    } catch (InterruptedException | ExecutionException e) {
      log.error("Something went wrong while receiving the dstat shell command outputs.");
      return false;
    }
  }
}
