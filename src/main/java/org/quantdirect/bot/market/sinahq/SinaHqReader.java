package org.quantdirect.bot.market.sinahq;

import com.google.gson.reflect.TypeToken;
import org.quantdirect.bot.market.Candle;
import org.quantdirect.bot.tool.TOOLS;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SinaHqReader extends HqReader {
    private final static Random rand = new Random();
    private final static DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static String dayUrl = "https://stock2.finance.sina.com.cn/futures/api/jsonp.php/var%20{var_name}=/InnerFuturesNewService.getDailyKLine?symbol={symbol}";
    private final static String fewUrl = "https://stock2.finance.sina.com.cn/futures/api/jsonp.php/var%20{var_name}=/InnerFuturesNewService.getFewMinLine?symbol={symbol}&type={few_minutes}";

    @Override
    public List<Candle> read(String instrumentId) {
        var str = httpResponse(instrumentId);
        var json = extractJson(str);
        return parse(json, instrumentId, true);
    }

    @Override
    public List<Candle> read(String instrumentId, int fewMinutes) {
        var str = httpResponse(instrumentId, fewMinutes);
        var json = extractJson(str);
        return parse(json, instrumentId, false);
    }

    private String extractJson(String str) {
        var i = str.indexOf("[");
        var i2 = str.lastIndexOf("]");
        if (i == -1 || i2 == -1) {
            throw new Error("No JSON array.");
        } else {
            return str.substring(i, i2+ 1);
        }
    }

    private List<Candle> parse(String json, String instrumentId, boolean isDay) {
        var r = parseResponse(json, isDay);
        r.forEach(c -> {
            c.setInstrumentId(instrumentId);
        });
        return r;
    }

    private List<Candle> parseResponse(String str, boolean isDay) {
        List<Candle> cd = new LinkedList<>();
        if (str == null || str.isBlank()) {
            return cd;
        }
        try {
            @SuppressWarnings("unchecked")
            var jc = (List<JsonCandle>) TOOLS.json().from(str, new TypeToken<List<JsonCandle>>() {
            }.getType());
            for (var j : jc) {
                var c = new Candle();
                c.setOpen(j.o);
                c.setHigh(j.h);
                c.setLow(j.l);
                c.setClose(j.c);
                c.setVolume(j.v);
                c.setPosition(j.p);
                c.setTime(parseTime(j.d, isDay));
                cd.add(c);
            }
            return cd;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private LocalDateTime parseTime(String d, boolean isDay) {
        if (d == null || d.isBlank()) {
            return null;
        }
        if (isDay) {
            var day =  LocalDate.parse(d, dayFmt);
            return LocalDateTime.of(day, LocalTime.of(0, 0));
        } else {
            return LocalDateTime.parse(d, timeFmt);
        }
    }

    private String httpResponse(String instrumentId, int fewMinutes) {
        var url = fewUrl.replace("{var_name}", fewVar(instrumentId, fewMinutes))
                        .replace("{symbol}", symbol(instrumentId))
                        .replace("{few_minutes}", Integer.toString(fewMinutes));
        return httpGet(url);
    }

    private String httpResponse(String instrumentId) {
        var url = dayUrl.replace("{var_name}", var(instrumentId))
                        .replace("{symbol}", symbol(instrumentId));
        return httpGet(url);
    }

    private String httpGet(String url) {
        var c = HttpClient.newHttpClient();
        var q = HttpRequest.newBuilder(URI.create(url))
                           .timeout(Duration.ofSeconds(15))
                           .GET().build();
        try {
            return c.send(q, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            throw new Error("HTTP IO error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new Error("HTTP GET timeout.", e);
        }
    }

    private String symbol(String instrumentId) {
        return instrumentId.toUpperCase(Locale.ROOT);
    }

    private String fewVar(String instrumentId, int few) {
        return "_" + symbol(instrumentId) + "_" + few + "_" + (1618633537208L + rand.nextLong() % 1000);
    }

    private String var(String instrumentId) {
        var n = LocalDate.now();
        return "_" + symbol(instrumentId) + n.getYear() + "_" + n.getMonthValue() + "_" + n.getDayOfMonth();
    }

    static class JsonCandle {
        String d;
        Double o;
        Double h;
        Double l;
        Double c;
        Long v;
        Long p;
    }
}
