package com.dudu.workflow;

import com.dudu.rest.model.BaiduWeatherResponse;
import com.dudu.workflow.common.RequestFactory;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;

import rx.observables.BlockingObservable;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_split() throws Exception {
        String s = "$411+OK";
        String sss[] = s.split("\\+");
        assertEquals(sss.length, 2);
    }

    @Test
    public void test_getHistoryWeather() throws Exception {
        BlockingObservable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> response
                = RequestFactory.getWeatherRequest().getYesterDayWeather("深圳").toBlocking();
        BaiduWeatherResponse.BaiduWeatherResult.HistoryResult historyResult = response.first();
        assertEquals(historyResult.week, "星期三");
    }

    @Test
    public void test_ByteBuffer() throws Exception {
        byte[] s1 = {0x63, 0x00, 0x02, 0x00, 0x00, 0x10, (byte) 0xf0, 0x5d, (byte) 0x4b, 0x03, (byte) 0xc2};
        byte[] s2 = {0x63, 0x02, 0x00, 0x00, 0x10, 0x00, 0x64, 0x4b, 0x03, (byte) 0xc2};
        ByteBuffer b1 = ByteBuffer.wrap(s1);
//        ByteBuffer b2 = ByteBuffer.wrap(s2, 1, s2.length - 1);
        System.out.println(String.copyValueOf(Hex.encodeHex(b1.array())));
//        System.out.println(String.copyValueOf(Hex.encodeHex(b2.array())));
        assertEquals(0x63, b1.get());
        assertEquals(0x00, b1.get());
        assertEquals(0x02, b1.get());
        assertEquals(0x00, b1.get());
        assertEquals(0x0010, b1.getShort());
        float press = (float) ((0x03ff & b1.getShort()) * 0.025);
        BigDecimal bg = new BigDecimal(press).setScale(3, RoundingMode.HALF_UP);
        System.out.println(bg);
        assertEquals(2.5, press, 0.001);
        assertEquals(80, (0x0ff & b1.get()) - 50);
    }

    @Test
    public void test_double() {
        double d = 0.01;
        boolean flag = d > 0;
        assertEquals(flag, true);
    }
}