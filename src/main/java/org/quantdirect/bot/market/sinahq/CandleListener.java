package org.quantdirect.bot.market.sinahq;

import org.quantdirect.bot.market.Candle;
import org.quantdirect.bot.market.MarketSource;

import java.util.Collection;
import java.util.List;

public abstract class CandleListener {
    public void onCandle(int fewMinutes, MarketSource source, List<Candle> candles) {}

    public void onError(Throwable throwable) {};
}
