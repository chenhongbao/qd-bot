package org.quantdirect.bot.market;

import org.quantdirect.bot.tool.TOOLS;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

public abstract class Market {

    private static CtpMarket m;

    public static Market createCtp(String flowPath, boolean udp, boolean multicast, MarketListener listener, String[] args) throws TimeoutException, IOException {
        if (m == null) {
            var path = TOOLS.validateFlowPath(flowPath);
            m = new CtpMarket(path, udp, multicast, listener, args);
        }
        return m;
    }

    public abstract void addListener(MarketListener listener);

    public abstract void subscribe(String... instrumentId);

    public abstract void join();

    public abstract LocalDate getTradingDay();
}
