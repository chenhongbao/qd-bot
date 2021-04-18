package org.quantdirect.bot.market.sinahq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quantdirect.bot.market.Candle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SinaHqTest {

    private final SinaHqReader sina = new SinaHqReader();
    private final SinaHqCache cache = new SinaHqCache();

    @Test
    @DisplayName("Day K Line")
    public void testRead1() {
        final var instrumentId = "c2109";
        var cs = sina.read(instrumentId);
        checkAvailability(cs, instrumentId);
    }

    @Test
    @DisplayName("Few Minutes K Line")
    public void testRead2() {
        final var instrumentId = "RM2109";
        final var few = 60;
        var cs = sina.read(instrumentId, few);
        checkAvailability(cs, instrumentId);
    }

    @Test
    @DisplayName("Cache Read/Write")
    public void testCache() {
        final var instrumentId = "rb2110";
        final var few = 15;
        var cs = sina.read(instrumentId, few);
        cache.write(cs, instrumentId, few);

        var cs0 = cache.read(instrumentId, few);
        assertEquals(cs.size(), cs0.size());
        var index = 0;
        for (; index < cs.size(); ++index) {
            var c = cs.get(index);
            var c0 = cs0.get(index);
            assertEquals(c.getTime(), c0.getTime());
        }
    }

    private void checkAvailability(List<Candle> cs, String instrumentId) {
        assertTrue(!cs.isEmpty());
        for (var c : cs) {
            assertTrue(c.getInstrumentId().equals(instrumentId));
            assertTrue(c.getTime() != null);
            assertTrue(c.getOpen() != null);
            assertTrue(c.getHigh() != null);
            assertTrue(c.getLow() != null);
            assertTrue(c.getClose() != null);
            assertTrue(c.getVolume() != null);
            assertTrue(c.getPosition() != null);
        }
    }
}