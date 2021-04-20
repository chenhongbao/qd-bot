package org.quantdirect.bot.tool;

import org.quantdirect.bot.market.Tick;

class TickMinute {
    private Tick t;
    private long pastMin;

    TickMinute(Tick tick, long past) {
        if (tick == null) {
            throw new Error("Tick param null.");
        }
        t = tick;
        pastMin = past;
    }

    Tick tick() {
        return t;
    }

    void tick(Tick tick) {
        t = tick;
    }

    long minutes() {
        return pastMin;
    }

    void minutes(long m) {
        pastMin = m;
    }
}
