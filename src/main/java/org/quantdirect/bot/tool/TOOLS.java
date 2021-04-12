package org.quantdirect.bot.tool;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class TOOLS {
    private final static AtomicInteger ai = new AtomicInteger(0);
    private final static DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final static DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static JSON json;

    public synchronized static JSON json() {
        if (json == null) {
            json = new JSON();
        }
        return json;
    }

    public static int nextRequestId() {
        return ai.incrementAndGet();
    }

    public static void mkdir(String dir) {
        var p = Paths.get(dir);
        if (!Files.exists(p) || !Files.isDirectory(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                log(e, "TOOL");
            }
        }
    }

    public static String formatDouble(double d) {
        if (d == Double.MAX_VALUE) {
            return "Double.MAX_VALUE";
        } else if (d == Double.MIN_VALUE) {
            return "Double.MIN_VALUE";
        } else {
            return String.format("%.2f", d);
        }
    }

    public static String validateFlowPath(String path) {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            return path + "/";
        } else {
            return path;
        }
    }

    public static LocalDate toTradingDay(String tradingDay) {
        try {
            return LocalDate.parse(tradingDay, dayFmt);
        } catch (Throwable throwable) {
            log(throwable, TOOLS.class);
            return null;
        }
    }

    public static String toTradingDay(LocalDate tradingDay) {
        return tradingDay.format(dayFmt);
    }

    public static LocalDateTime toUpdateTime(String actionDay, String updateTime, int updateMillisec) {
        try {
            var day = LocalDate.parse(actionDay, dayFmt);
            var time = LocalTime.parse(updateTime, timeFmt);
            var r = LocalDateTime.of(day, time).plus(Duration.ofMillis(updateMillisec));
            return r;
        } catch (Throwable throwable) {
            log(throwable, TOOLS.class);
            return null;
        }
    }

    public static void writeText(String filename, String text) {
        try (FileWriter fw = new FileWriter(filename, false)) {
            fw.write(text);
        } catch (IOException e) {
            log(e, "TOOLS");
        }
    }

    public static String readText(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            var l = br.readLine();
            if (l != null) {
                return l.trim();
            } else {
                return "";
            }
        } catch (IOException e) {
            log(e, "TOOLS");
            return "";
        }
    }

    public static void log(String message, Object source) {
        try (PrintWriter pw = pw()) {
            log(pw, source);
            pw.write(message + "\n");
            pw.flush();
        } catch (IOException exception) {
            throw new Error("Can't create log file.", exception);
        }
    }

    public static void log(Throwable throwable, Object source) {
        try (PrintWriter pw = pw()) {
            log(pw, source);
            throwable.printStackTrace(pw);
            pw.write("\n");
            pw.flush();
        } catch (IOException exception) {
            throw new Error("Can't create log file.", exception);
        }
    }

    private static PrintWriter pw() throws IOException {
        return new PrintWriter(new FileWriter(TOOLS.class.getSimpleName(), true));
    }

    private static void log(PrintWriter pw, Object source) {
        pw.write("[" + LocalDateTime.now() + "]");
        pw.write("[" + source + "]\n");
    }

    public static class JSON {
        private final Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        public <T> T from(File json, Class<T> clazz) throws IOException {
            try (FileReader fr = new FileReader(json)) {
                return gson.fromJson(fr, clazz);
            }
        }

        public <T> T from(String json, Class<T> clazz) throws IOException {
            return gson.fromJson(json, clazz);
        }

        public String to(Object o) {
            return gson.toJson(o);
        }
    }
}
