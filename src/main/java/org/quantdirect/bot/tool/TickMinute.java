package org.quantdirect.bot.tool;

import org.quantdirect.bot.market.Tick;

public class TickMinute {
    private Tick t;
    private long pastMin;

    public TickMinute(Tick tick, long past) {
        if (tick == null) {
            throw new Error("Tick param null.");
        }
        t = tick;
        pastMin = past;
    }

    public Tick tick() {
        return t;
    }

    public void tick(Tick tick) {
        t = tick;
    }

    public long minutes() {
        return pastMin;
    }

    public void minutes(long m) {
        pastMin = m;
    }
}
