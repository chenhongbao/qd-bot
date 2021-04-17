package org.quantdirect.bot.market.sinahq;

import org.quantdirect.bot.market.Candle;
import org.quantdirect.bot.market.MarketSource;

public abstract class CandleListener {
    public void onCandle(Candle candle, int fewMinutes, MarketSource source) {}

    public void onError(Throwable throwable) {};
}
