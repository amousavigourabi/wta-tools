package com.asml.apa.wta.core.supplier;

import com.asml.apa.wta.core.dto.DstatDto;
import com.asml.apa.wta.core.utils.BashUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DstatSupplierTest {


    @Test
    void successfullyCreateDstatDto() throws ExecutionException, InterruptedException {
    //TODO: wait for henry's branch to get merged and make sure assertEquals passes (by passing in timestamp)
        BashUtils bashUtils = Mockito.mock(BashUtils.class);
        Mockito.doReturn(CompletableFuture.completedFuture(
        "----total-usage---- -dsk/total- -net/total- ---paging-- ---system--\n" +
            "usr sys idl wai stl| read  writ| recv  send|  in   out | int   csw\n" +
            "  0   1  98   0   0|   0     0 |   0     0 |   0     0 | 516  2116"))
                .when(bashUtils)
                .executeCommand("dstat -cdngy 1 -c 1");
        DstatSupplier sut = Mockito.spy(new DstatSupplier(bashUtils));

        var actual = sut.getAllMetrics("x1");

        DstatDto expected = DstatDto.builder()
        .totalUsageUsr(0)
        .totalUsageSys(1)
        .totalUsageIdl(98).totalUsageWai(0).totalUsageStl(0)
        .dskRead(0).dskWrite(0).netRecv(0).netSend(0).pagingIn(0).pagingOut(0).systemInt(516).systemCsw(2116).build();

        assertEquals(expected, actual);
    }
}
