package org.quantdirect.bot.market.sinahq;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.quantdirect.bot.tool.TOOLS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    public void testJsonArray() {
        List<SinaHqReader.JsonCandle> cs = new LinkedList<>();
        var str = readJson("few_minutes.json");
        assertDoesNotThrow(() -> {
            var type = new TypeToken<List<SinaHqReader.JsonCandle>>() {
            }.getType();
            cs.addAll(TOOLS.json().from(str, type));
        });
        assertTrue(cs.size() > 0);
        var x = cs.get(cs.size() - 1);
        assertEquals(x.d, "2021-04-16 23:00:00");
        assertEquals(x.o, 2733D);
        assertEquals(x.h, 2738D);
        assertEquals(x.l, 2733D);
        assertEquals(x.c, 2738D);
        assertEquals(x.v, 2740);
        assertEquals(x.p, 209356);
    }

    private String readJson(String file) {
        var s = this.getClass().getResourceAsStream(file);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s))) {
            StringBuffer buf = new StringBuffer();
            String str = br.readLine();
            while (str != null) {
                buf.append(str);
                str = br.readLine();
            }
            return buf.toString();
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }
}