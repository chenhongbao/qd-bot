package org.quantdirect.bot.market;

import org.quantdirect.bot.market.sinahq.CandleListener;

import java.util.List;

class ParentCandleListener extends CandleListener {
    private final CtpMarketSpi  spi;

    ParentCandleListener(CtpMarketSpi spi) {
        this.spi = spi;
    }

    @Override
    public synchronized void onCandle(int fewMinutes, MarketSource source, List<Candle> candles) {
        if (candles.isEmpty()) {
            return;
        }
        spi.callCandle(fewMinutes, source, candles.toArray(new Candle[0]));
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        spi.callError(throwable);
    }
}