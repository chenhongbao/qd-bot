package org.quantdirect.bot.market;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CtpMarketTest {

    @Test
    public void testApp() {
        try {
            final CountDownLatch cdl = new CountDownLatch(1);
            final Map<Integer, List<Candle>> candles = new HashMap<>();
            var market = Market.createCtp("market/", true, true, new MarketListener() {
                @Override
                public void onCandle(Candle candle, int fewMinutes, MarketSource source) {
                    candles.computeIfAbsent(fewMinutes, k -> new LinkedList<>()).add(candle);
                    assertEquals(source, MarketSource.OFFLINE);
                    cdl.countDown();
                }

                @Override
                public void onLogin(Market market) {
                    market.subscribeCandle("c2109");
                }
            }, new String[] {"c2109"});

            cdl.await(5, TimeUnit.SECONDS);
            assertTrue(!candles.isEmpty());
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }
}