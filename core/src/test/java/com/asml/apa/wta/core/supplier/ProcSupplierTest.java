package com.asml.apa.wta.core.supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.asml.apa.wta.core.dto.ProcDto;
import com.asml.apa.wta.core.utils.ShellUtils;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProcSupplierTest {
  @Test
  void getSnapshotReturnsProcDto() {
    ShellUtils shellUtils = Mockito.mock(ShellUtils.class);
    doReturn(CompletableFuture.completedFuture("8       0 sda 1114 437 141266 153\n"
            + "8      16 sdb 103 0 4712 174\n" + "8      32 sdc 77636 10312 5307586 5345"))
        .when(shellUtils)
        .executeCommand("cat /proc/diskstats");

    doReturn(CompletableFuture.completedFuture("MemTotal:       10118252 kB\n" + "MemFree:         1921196 kB\n"
            + "MemAvailable:    5470300 kB\n"
            + "Buffers:          239068 kB\n"))
        .when(shellUtils)
        .executeCommand("cat /proc/meminfo");

    doReturn(CompletableFuture.completedFuture("Intel(R) Core(TM) i7-10750H CPU @ 2.60GHz"))
        .when(shellUtils)
        .executeCommand("grep -m 1 \"model name\" /proc/cpuinfo | awk -F: '{print $2}' | sed 's/^[ \\t]*//'");

    doReturn(CompletableFuture.completedFuture("0,62 1.23 1.02 1/479 278339"))
        .when(shellUtils)
        .executeCommand("cat /proc/loadavg");
    ProcSupplier sut = new ProcSupplier(shellUtils);

    ProcDto expected = ProcDto.builder()
        .readsCompleted(78853L)
        .readsMerged(10749L)
        .sectorsRead(5453564L)
        .timeSpentReading(5672L)
        .memTotal(10118252L)
        .memFree(1921196L)
        .memAvailable(5470300L)
        .buffers(239068L)
        .cpuModel("Intel(R Core(TM) i7-10750H CPU @ 2.60GHz")
        .loadAvgOneMinute(0.62)
        .loadAvgFiveMinutes(1.23)
        .loadAvgFifteenMinutes(1.02)
        .numberOfExecutingKernelSchedulingEntities(1.0)
        .numberOfExistingKernelSchedulingEntities(479.0)
        .pIdOfMostRecentlyCreatedProcess(278339.0)
        .build();

    if (sut.isAvailable()) {
      assertEquals(expected, sut.getSnapshot().join().get());
    } else {
      assertThat(sut.getSnapshot().join()).isEmpty();
    }
  }

  @Test
  void emptyOutputReturnEmptyProcDto() {
    ShellUtils shellUtils = Mockito.mock(ShellUtils.class);
    doReturn(CompletableFuture.completedFuture("")).when(shellUtils).executeCommand("cat /proc/diskstats");

    doReturn(CompletableFuture.completedFuture("")).when(shellUtils).executeCommand("cat /proc/meminfo");

    doReturn(CompletableFuture.completedFuture(""))
        .when(shellUtils)
        .executeCommand("grep -m 1 \"model name\" /proc/cpuinfo | awk -F: '{print $2}' | sed 's/^[ \\t]*//'");

    doReturn(CompletableFuture.completedFuture("")).when(shellUtils).executeCommand("cat /proc/loadavg");
    ProcSupplier sut = new ProcSupplier(shellUtils);

    ProcDto expected = ProcDto.builder().build();

    if (sut.isAvailable()) {
      assertEquals(expected, sut.getSnapshot().join().get());
    } else {
      assertThat(sut.getSnapshot().join()).isEmpty();
    }
  }
}
