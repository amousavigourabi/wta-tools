package com.asml.apa.wta.core.supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.asml.apa.wta.core.dto.DstatDto;
import com.asml.apa.wta.core.utils.BashUtils;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class DstatSupplierIntegrationTest {
  @Test
  public void DstatSupplierSuccessfullyReturnsADtoObject() throws ExecutionException, InterruptedException {
    DstatSupplier a = new DstatSupplier(new BashUtils());
    if (a.isAvailable()) {
      var actual = a.getSnapshot().get();
      assertTrue(actual instanceof DstatDto);
    }
  }
}
