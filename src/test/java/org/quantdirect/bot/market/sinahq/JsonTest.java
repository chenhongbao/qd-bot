package org.quantdirect.bot.market.sinahq;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.quantdirect.bot.tool.TOOLS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class JsonTest {

    @Test
    public void testJsonArray() {
        var str = readJson("few_minutes.json");
        try {
            var cs = (List<SinaHqReader.JsonCandle>)TOOLS.json().from(str, new TypeToken<List<SinaHqReader.JsonCandle>>(){}.getType());
            for (var a : cs) {
                System.out.println(a.d + ": " + a.c);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
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