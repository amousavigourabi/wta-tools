package com.asml.apa.wta.core.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.asml.apa.wta.core.dto.IostatDto;
import com.asml.apa.wta.core.utils.BashUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IostatSupplierTest {

  @Test
  public void getAllMetricsReturnsIostatDto() {
    BashUtils bashUtils = Mockito.mock(BashUtils.class);
    Mockito.doReturn(CompletableFuture.completedFuture("str"))
        .when(bashUtils)
        .executeCommand("iostat");
    IostatSupplier sut = Mockito.spy(new IostatSupplier(bashUtils));

    Mockito.doReturn(CompletableFuture.completedFuture("str 1.0 2.0 3.0 4.0 5.0 6.0 7.0"))
        .when(bashUtils)
        .executeCommand("iostat -d | awk '$1 == \"sdc\"'");

    IostatDto expected = IostatDto.builder()
        .tps(1.0)
        .kiloByteReadPerSec(2.0)
        .kiloByteWrtnPerSec(3.0)
        .kiloByteDscdPerSec(4.0)
        .kiloByteRead(5.0)
        .kiloByteWrtn(6.0)
        .kiloByteDscd(7.0)
        .build();

    IostatDto result = sut.getSnapshot().join();

    // Assert everything field other than the timestamp is the same
    assertEquals(expected.getTps(), result.getTps());
    assertEquals(expected.getKiloByteReadPerSec(), result.getKiloByteReadPerSec());
    assertEquals(expected.getKiloByteWrtnPerSec(), result.getKiloByteWrtnPerSec());
    assertEquals(expected.getKiloByteDscdPerSec(), result.getKiloByteDscdPerSec());
    assertEquals(expected.getKiloByteRead(), result.getKiloByteRead());
    assertEquals(expected.getKiloByteWrtn(), result.getKiloByteWrtn());
    assertEquals(expected.getKiloByteDscd(), result.getKiloByteDscd());
  }
}
