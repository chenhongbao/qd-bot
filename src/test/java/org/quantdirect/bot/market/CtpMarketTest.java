package org.quantdirect.bot.market;

import org.junit.jupiter.api.*;
import org.quantdirect.bot.tool.TOOLS;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CtpMarketTest {

    @Test
    @DisplayName("Initial Candle Fetch")
    //@Disabled /* Enable this test only when market is open. */
    public void testApp() throws IOException, TimeoutException, InterruptedException {
        final String instrumentId = "c2109";
        final CountDownLatch cdl = new CountDownLatch(6);
        final Map<Integer, List<Candle>> candles = new ConcurrentHashMap<>();
        var market = Market.createCtp("market/", true, true, new MarketListener() {
            @Override
            public void onCandle(int fewMinutes, MarketSource source, Candle candle, boolean isLast) {
                assertEquals(source, MarketSource.OFFLINE);
                checkCandle(candle, instrumentId);
                synchronized (candles) {
                    if (isLast) {
                        cdl.countDown();
                    }
                    var p = candles.computeIfAbsent(fewMinutes, k -> new LinkedList<>());
                    if (!p.isEmpty()) {
                        // Check sorted.
                        var pc = p.get(p.size() - 1);
                        assertTrue(pc.getTime().isBefore(candle.getTime()));
                    }
                    p.add(candle);
                }
            }

            @Override
            public void onLogin(Market market) {
                market.subscribeCandle(instrumentId);
            }
        }, new String[]{instrumentId});

        cdl.await(15, TimeUnit.SECONDS);
        assertTrue(candles.size() == 6);
        for (var cs : candles.values()) {
            assertTrue(!cs.isEmpty());
        }
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