package org.quantdirect.bot.market.sinahq;

import org.quantdirect.bot.market.Candle;
import org.quantdirect.bot.market.MarketSource;
import org.quantdirect.bot.tool.TOOLS;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HqMonitor implements Runnable {

    private final String id;
    private final long itvl;
    private final CandleListener candle;
    private final SinaHqReader online;
    private final SinaHqCache offline;
    private final HqMerger dayMerger;
    private final Map<Integer, HqMerger> fewMergers;
    private final Thread deam;
    private boolean notInited;

    public HqMonitor(String instrumentId, int interval, TimeUnit unit, CandleListener listener) {
        id = instrumentId;
        itvl = unit.toMillis(interval);
        candle = listener;
        online = new SinaHqReader();
        offline = new SinaHqCache();
        dayMerger = new HqMerger();
        fewMergers = new ConcurrentHashMap<>();
        fewMergers.put(1, new HqMerger());
        fewMergers.put(5, new HqMerger());
        fewMergers.put(15, new HqMerger());
        fewMergers.put(30, new HqMerger());
        fewMergers.put(60, new HqMerger());
        deam = new Thread(this);
        deam.start();
        notInited = true;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                updateDay();
                for (var few : fewMergers.keySet()) {
                    try {
                        updateFew(few);
                    } catch (Throwable throwable) {
                        TOOLS.log(throwable, this);
                    }
                }
                inited();
                Thread.sleep(itvl);
            } catch (Throwable throwable) {
                TOOLS.log(throwable, this);
            }
        }
    }

    private void updateDay() {
        updateDay(offline, MarketSource.OFFLINE);
        updateDay(online, onlineSwitcher());
    }

    private void updateDay(HqReader hq, MarketSource source) {
        List<Candle> update = new LinkedList<>();
        try {
            update = hq.read(id);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
        var dif = dayMerger.merge(update);
        sortAndCall(dif, 0, source);
    }

    private void updateFew(int few) {
        updateFew(few, offline, MarketSource.OFFLINE);
        updateFew(few, online, onlineSwitcher());
    }

    private MarketSource onlineSwitcher() {
        if (notInited) {
            return MarketSource.OFFLINE;
        } else {
            return MarketSource.ONLINE;
        }
    }

    private void inited() {
        if (notInited) {
            notInited = false;
        }
    }

    private void updateFew(int few, HqReader hq, MarketSource source) {
        var m = getMerger(few);
        List<Candle> update = new LinkedList<>();
        try {
            update.addAll(hq.read(id, few));
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
        var dif = m.merge(update);
        sortAndCall(dif, few, source);
    }

    private HqMerger getMerger(int few) {
        var m = fewMergers.get(few);
        if (m == null) {
            throw new Error("No such few minutes: " + few + ".");
        }
        return m;
    }

    private void sortAndCall(List<Candle> dif, int few, MarketSource source) {
        dif.sort(Comparator.comparing(Candle::getTime));
        for (var c : dif) {
            try {
                candle.onCandle(c, few, source);
            } catch (Throwable throwable) {
                TOOLS.log(throwable, this);
                try {
                    candle.onError(throwable);
                } catch (Throwable ignored) {
                }
            }
        }
        if (!dif.isEmpty()) {
            if (few == 0) {
                offline.write(dayMerger.getCache(), id);
            } else {
                offline.write(getMerger(few).getCache(), id, few);
            }
        }
    }
}
