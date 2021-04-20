package org.quantdirect.bot.tool;

import org.quantdirect.bot.market.Tick;
import org.quantdirect.bot.trader.Order;
import org.quantdirect.bot.trader.Trade;
import org.quantdirect.bot.trader.TradeListener;
import org.quantdirect.bot.trader.Trader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

public class TradeKeeper {

    private final String fn = "trades";
    private final Deque<TradeRecord> records;
    private final Trader trader;
    private boolean enabled;

    public TradeKeeper() {
        records = new LinkedList<>();
        trader = create();
        enabled = false;
    }

    public void enable(boolean b) {
        enabled = b;
    }

    private Trader create() {
        try {
            return Trader.createCtp("trader/");
        } catch (TimeoutException e) {
            throw new Error("Trader connection timeout.", e);
        } catch (IOException e) {
            throw new Error("Flow files error.", e);
        }
    }

    public TradeRecord current() {
        try {
            return records.getFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public synchronized void bo(Tick tick, long quantity) {
        TOOLS.log("BO/" + TOOLS.formatDouble(tick.getAskPrice()) + "/" +
                  TOOLS.formatDouble(tick.getBidPrice()), this);
        open(tick, Direction.BUY, quantity);
    }

    public synchronized void so(Tick tick, long quantity) {
        TOOLS.log("SO/" + TOOLS.formatDouble(tick.getAskPrice()) + "/" +
                  TOOLS.formatDouble(tick.getBidPrice()), this);
        open(tick, Direction.SELL, quantity);
    }

    public synchronized void sc(Tick tick, Direction direction) {
        TOOLS.log("SC/" + TOOLS.formatDouble(tick.getAskPrice()) + "/" +
                  TOOLS.formatDouble(tick.getBidPrice()), this);
        close(tick, direction);
    }

    public synchronized void bc(Tick tick, Direction direction) {
        TOOLS.log("BC/" + TOOLS.formatDouble(tick.getAskPrice()) + "/" +
                  TOOLS.formatDouble(tick.getBidPrice()), this);
        close(tick, direction);
    }

    private void open(Tick tick, Direction direction, long quantity) {
        setOpenRecord(tick, direction, quantity);
        if (enabled) {
            tradeOpen();
        }
    }

    private void tradeOpen() {
        var direction = current().direction();
        var tick = current().open();
        var quantity = current().quantity();
        if (direction == Direction.BUY) {
            trader.buyOpen(tick.getInstrumentId(), tick.getExchangeId(),
                    tick.getUpperLimitPrice(), (int)quantity, new PrivateTraderListener());
        } else if (direction == Direction.SELL) {
            trader.sellOpen(tick.getInstrumentId(), tick.getExchangeId(),
                    tick.getLowerLimitPrice(), (int)quantity, new PrivateTraderListener());
        } else {
            throw new Error("Invalid direction for open trade: " + direction + ".");
        }
    }

    private void setOpenRecord(Tick tick, Direction direction, long quantity) {
        if (!records.isEmpty()) {
            // Check previous trade is finished.
            var r = records.getFirst();
            if (r.close() == null) {
                throw new Error("Previous trade is not closed yet.");
            }
        }
        var n = new TradeRecord(direction, quantity);
        n.open(tick);
        records.addFirst(n);
        write();
    }

    private void close(Tick tick, Direction direction) {
        setCloseRecord(tick, direction);
        if (enabled) {
            tradeClose();
        }
    }

    private void tradeClose() {
        var tick = current().close();
        var quantity = current().quantity();
        var d = records.getFirst().direction();
        if (d == Direction.BUY) {
            trader.sellClose(tick.getInstrumentId(), tick.getExchangeId(),
                    tick.getBidPrice(), (int)quantity, true, new PrivateTraderListener());
        } else if (d == Direction.SELL) {
            trader.buyClose(tick.getInstrumentId(), tick.getExchangeId(),
                    tick.getAskPrice(), (int)quantity, true, new PrivateTraderListener());
        } else {
            throw new Error("Invalid direction for open trade: " + d + ".");
        }
    }

    private void setCloseRecord(Tick tick, Direction direction) {
        if (current() == null) {
            throw new Error("No trade to close.");
        }
        var n = current();
        if (n.close() != null) {
            throw new Error("Close the same trade again.");
        }
        if (n.open() == null) {
            throw new Error("Close a trade that has no open tick.");
        }
        if (n.direction() == direction) {
            throw new Error("Close trade at wrong direction.");
        }
        n.close(tick);
        write();
    }

    private void write() {
        try (FileWriter fw = new FileWriter(fn, false)) {
            var str = TOOLS.json().to(records);
            fw.write(str);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private class PrivateTraderListener extends TradeListener {

        @Override
        public void onOrder(Order order) {
        }

        @Override
        public void onTrade(Trade trade) {
            TOOLS.log(TOOLS.formatDouble(trade.getPrice()) + "/" + trade.getQuantity(), this);
        }
    }
}
