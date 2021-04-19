package org.quantdirect.bot.market;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quantdirect.bot.tool.TOOLS;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class CtpMarketTest2 {
    @Test
    @DisplayName("Runtime Candle Update")
    public void testRuntimeApp() throws IOException, TimeoutException, InterruptedException {
        final String instrumentId = "rb2110";
        var m = Market.createCtp("market/", true, true, new MarketListener() {
            @Override
            public void onCandle(int fewMinutes, MarketSource source, Candle candle, boolean isLast) {
                if (source == MarketSource.ONLINE) {
                    System.out.println("ONLINE[" + fewMinutes + "]: " + TOOLS.json().to(candle));
                } else {
                    System.out.println("OFFLINE[" + fewMinutes + "]: " + candle.getTime());
                }
                checkCandle(candle, instrumentId);
            }

            @Override
            public void onLogin(Market market) {
                market.subscribeCandle(instrumentId);
            }
        }, new String[]{instrumentId});
        new CountDownLatch(1).await();
    }

    private void checkCandle(Candle c, String instrumentId) {
        assertTrue(c != null);
        // Check candle.
        assertEquals(c.getInstrumentId(), instrumentId);
        assertTrue(c.getTime() != null);
        assertTrue(c.getOpen() != null);
        assertTrue(c.getHigh() != null);
        assertTrue(c.getLow() != null);
        assertTrue(c.getClose() != null);
        assertTrue(c.getVolume() != null);
        assertTrue(c.getPosition() != null);
    }
}