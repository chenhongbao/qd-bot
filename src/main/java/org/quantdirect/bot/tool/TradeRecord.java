package org.quantdirect.bot.tool;

import org.quantdirect.bot.market.Tick;

public class TradeRecord {

    private Tick open;
    private Tick close;
    private final Direction d;
    private final long q;

    public TradeRecord(Direction direction, long quantity) {
        if (direction != Direction.BUY && direction != Direction.SELL) {
            throw new Error("Invalid direction for open trade: " + direction + ".");
        }
        d = direction;
        q = quantity;
    }

    public long quantity() {
        return q;
    }

    public void open(Tick tick) {
        open = tick;
    }

    public void close(Tick tick) {
        close = tick;
    }

    public Tick open() {
        return open;
    }

    public Tick close() {
        return close;
    }

    public Direction direction() {
        return d;
    }
}
