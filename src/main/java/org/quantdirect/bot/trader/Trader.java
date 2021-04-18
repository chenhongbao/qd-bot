package org.quantdirect.bot.trader;

import org.ctp4j.ThostFtdcCtpApi;
import org.quantdirect.bot.tool.TOOLS;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

public abstract class Trader {

    private static CtpTrader t;

    public static synchronized Trader createCtp(String flowPath) throws TimeoutException, IOException {
        if (t == null) {
            ThostFtdcCtpApi.install();
            var path = TOOLS.validateFlowPath(flowPath);
            t = new CtpTrader(path);
        }
        return t;
    }

    public abstract void buyOpen(String instrumentId, String exchangeId, double price, int quantity);

    public abstract void buyOpen(String instrumentId, String exchangeId, double price, int quantity, TradeListener listener);

    public abstract void buyClose(String instrumentId, String exchangeId, double price, int quantity, boolean today);

    public abstract void buyClose(String instrumentId, String exchangeId, double price, int quantity, boolean today, TradeListener listener);

    public abstract void sellOpen(String instrumentId, String exchangeId, double price, int quantity);

    public abstract void sellOpen(String instrumentId, String exchangeId, double price, int quantity, TradeListener listener);

    public abstract void sellClose(String instrumentId, String exchangeId, double price, int quantity, boolean today);

    public abstract void sellClose(String instrumentId, String exchangeId, double price, int quantity, boolean today, TradeListener listener);

    public abstract boolean isAvailable();

    public abstract LocalDate getTradingDay();
}
