package org.quantdirect.bot.market;

public abstract class MarketListener {
    public void onTick(Tick tick) {}

    public void onCandle(Candle candle, int fewMinutes, MarketSource source){}

    public void onLogin(Market market) {}

    public void onDisconnected(int reason) {}

    public void onError(Throwable throwable) {}

    public void onInit(String[] args) {}
}
