package org.quantdirect.bot.tool;

import org.quantdirect.bot.market.Tick;

public class TradeRecord {

    private Tick open;
    private Tick close;
    private final Direction d;

    public TradeRecord(Direction direction) {
        if (direction != Direction.BUY && direction != Direction.SELL) {
            throw new Error("Invalid direction for open trade: " + direction + ".");
        }
        d = direction;
    }

    void open(Tick tick) {
        open = tick;
    }

    void close(Tick tick) {
        close = tick;
    }

    Tick open() {
        return open;
    }

    Tick close() {
        return close;
    }

    Direction direction() {
        return d;
    }
}
