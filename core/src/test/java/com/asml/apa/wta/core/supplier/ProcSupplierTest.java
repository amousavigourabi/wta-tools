package com.asml.apa.wta.core.supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.asml.apa.wta.core.dto.ProcDto;
import com.asml.apa.wta.core.utils.BashUtils;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ProcSupplierTest {
  @Test
  void getSnapshotReturnsProcDto() {
    BashUtils bashUtils = Mockito.mock(BashUtils.class);
    doReturn(CompletableFuture.completedFuture("8       0 sda 1114 437 141266 153\n"
            + "8      16 sdb 103 0 4712 174\n" + "8      32 sdc 77636 10312 5307586 5345"))
        .when(bashUtils)
        .executeCommand("cat /proc/diskstats");

    doReturn(CompletableFuture.completedFuture("MemTotal:       10118252 kB\n" + "MemFree:         1921196 kB\n"
            + "MemAvailable:    5470300 kB\n"
            + "Buffers:          239068 kB\n"))
        .when(bashUtils)
        .executeCommand("cat /proc/meminfo");

    doReturn(CompletableFuture.completedFuture("Intel(R) Core(TM) i7-10750H CPU @ 2.60GHz"))
        .when(bashUtils)
        .executeCommand("grep -m 1 \"model name\" /proc/cpuinfo | awk -F: '{print $2}' | sed 's/^[ \\t]*//'");

    doReturn(CompletableFuture.completedFuture("0,62 1.23 1.02 1/479 278339"))
        .when(bashUtils)
        .executeCommand("cat /proc/loadavg");
    ProcSupplier sut = new ProcSupplier(bashUtils);

    ProcDto expected = ProcDto.builder()
        .readsCompleted(Optional.of(78853L))
        .readsMerged(Optional.of(10749L))
        .sectorsRead(Optional.of(5453564L))
        .timeSpentReading(Optional.of(5672L))
        .memTotal(Optional.of(10118252L))
        .memFree(Optional.of(1921196L))
        .memAvailable(Optional.of(5470300L))
        .buffers(Optional.of(239068L))
        .cpuModel(Optional.of("Intel(R) Core(TM) i7-10750H CPU @ 2.60GHz"))
        .loadAvgOneMinute(Optional.of(0.62))
        .loadAvgFiveMinutes(Optional.of(1.23))
        .loadAvgFifteenMinutes(Optional.of(1.02))
        .numberOfExecutingKernelSchedulingEntities(Optional.of(1.0))
        .numberOfExistingKernelSchedulingEntities(Optional.of(479.0))
        .pIdOfMostRecentlyCreatedProcess(Optional.of(278339.0))
        .build();

    if (sut.isAvailable()) {
      assertEquals(expected, sut.getSnapshot().join());
    } else {
      assertThat(sut.getSnapshot().join()).isNull();
    }
  }
}
