package org.quantdirect.bot.market.sinahq;

import org.quantdirect.bot.market.Candle;

import java.util.List;

public abstract class HqReader {

    private static HqReader online, offline;

    public synchronized static HqReader online() {
        if (online == null) {
            online = new SinaHqReader();
        }
        return online;
    }

    public synchronized static HqReader offline() {
        if (offline == null) {
            offline = new SinaHqCache();
        }
        return offline;
    }

    public abstract List<Candle> read(String instrumentId);

    public abstract List<Candle> read(String instrumentId, int fewMinutes);
}
