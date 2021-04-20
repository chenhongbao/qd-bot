package org.quantdirect.bot.tool;

import com.google.gson.reflect.TypeToken;
import org.quantdirect.bot.market.Tick;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TickKeeper {

    private final static String fn = "ticks";
    private final Map<Integer, TickMinute> ticks;

    private LocalTime time;
    private TickMinute open;
    private TickMinute close;

    public TickKeeper() {
        ticks = new ConcurrentHashMap<>();
        read();
    }

    public void open(Tick tick) {
        open = new TickMinute(tick, 0);
    }

    public TickMinute open() {
        return open;
    }

    public void close(Tick tick) {
        close = new TickMinute(tick, 0);
    }

    public TickMinute close() {
        return close;
    }

    public TickMinute get(Integer key) {
        return ticks.get(key);
    }

    public TickMinute update(Tick tick) {
        updateMinutes(tick);
        return updateTick(tick);
    }

    private TickMinute updateTick(Tick tick) {
        Integer p = intValue(tick.getLastPrice());
        TickMinute prev = ticks.put(p, new TickMinute(tick, 0));
        if (prev != null && tick.getTimeStamp().isBefore(prev.tick().getTimeStamp())) {
            throw new Error("Current update time is before prev.");
        }
        write();
        return prev;
    }

    private void updateMinutes(Tick tick) {
        if (time != null && time.getMinute() != tick.getUpdateTime().getMinute() /* A new minute. */) {
            ticks.values().forEach(tm -> {
                tm.minutes(tm.minutes() + 1);
            });
            if (open != null) {
                open.minutes(open.minutes() + 1);
            }
            if (close != null) {
                close.minutes(close.minutes() + 1);
            }
        }
        time = tick.getUpdateTime();
    }

    public List<TickMinute> timeSortedTicks() {
        var r = new LinkedList<>(ticks.values());
        r.sort((o1, o2) -> o2.tick().getTimeStamp().compareTo(o1.tick().getTimeStamp()));
        return r;
    }

    private Integer intValue(Double p) {
        return p.intValue();
    }

    private void write() {
        var str = TOOLS.json().to(ticks);
        try (FileWriter fw = new FileWriter(fn, false)) {
            fw.write(str);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private void read() {
        var f = new File(fn);
        if (!f.exists()) {
            return;
        } else {
            try {
                Type t = new TypeToken<Map<Integer, TickMinute>>() {
                }.getType();
                ticks.putAll(TOOLS.json().from(f, t));
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }
}
