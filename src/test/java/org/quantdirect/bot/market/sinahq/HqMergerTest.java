package org.quantdirect.bot.market.sinahq;

import org.junit.jupiter.api.Test;
import org.quantdirect.bot.market.Candle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HqMergerTest extends JsonTest {

    private final static DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testIntersect() {
        loadCandles("few_minutes.json");

        // Intersect.
        var cs0 = create(5, 15);
        var cs1 = create(1, 10);
        var m = new HqMerger();

        var rn = m.merge(null);
        assertTrue(rn.isEmpty());

        var r0 = m.merge(cs0);
        assertEquals(r0.size(), 10);
        assertEquals(r0.size(), cs0.size());
        for (int i = 0; i < r0.size(); ++i) {
            assertEquals(r0.get(i).getTime(), cs0.get(i).getTime());
        }

        var r1 = m.merge(cs1);
        assertEquals(r1.size(), 4);
        // Test sorted.
        for (int i = 0; i < r1.size() - 1; ++i) {
            assertTrue(r1.get(i).getTime().isBefore(r1.get(i + 1).getTime()));
            assertTrue(r1.get(i).getTime().isAfter(cs0.get(9).getTime()));
            assertTrue(r1.get(i + 1).getTime().isAfter(cs0.get(9).getTime()));
        }
    }

    @Test
    public void testNonIntersect() {
        loadCandles("few_minutes.json");

        var cs0 = create(10, 15);
        var cs1 = create(1, 8);
        var m = new HqMerger();

        m.merge(cs0);
        var r1 = m.merge(cs1);
        assertEquals(r1.size(), 7);
        // Check sorted.
        for (int i = 0; i < r1.size() - 1; ++i) {
            assertTrue(r1.get(i).getTime().isBefore(r1.get(i + 1).getTime()));
            assertTrue(r1.get(i).getTime().isAfter(cs0.get(4).getTime()));
            assertTrue(r1.get(i + 1).getTime().isAfter(cs0.get(4).getTime()));
        }

        var cs2 = create(1, 3);
        var r2 = m.merge(cs2);
        assertTrue(r2.isEmpty());

        var c = m.getCache();
        assertEquals(c.size(), 12);
    }

    private List<Candle> create(int from, int to) {
        var r = new LinkedList<Candle>();
        for (int i = from; i < to; ++i) {
            var c = new Candle();
            var j = cs.get(cs.size() - i);
            c.setOpen(j.o);
            c.setHigh(j.h);
            c.setLow(j.l);
            c.setClose(j.c);
            c.setVolume(j.v);
            c.setPosition(j.p);
            c.setTime(parseTime(j.d, false));
            r.addFirst(c);
        }
        return r;
    }

    private LocalDateTime parseTime(String d, boolean isDay) {
        if (d == null || d.isBlank()) {
            return null;
        }
        if (isDay) {
            return LocalDateTime.parse(d, dayFmt);
        } else {
            return LocalDateTime.parse(d, timeFmt);
        }
    }
}