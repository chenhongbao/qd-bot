package org.quantdirect.bot.market;

import org.quantdirect.bot.market.Market;
import org.quantdirect.bot.market.Tick;

public interface MarketListener {
    void onTick(Tick tick);

    void onLogin(Market market);

    void onDisconnected(int reason);

    void onError(Throwable throwable);

    void onInit(String[] args);
}
