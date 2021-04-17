package org.quantdirect.bot.market;

import org.quantdirect.bot.market.sinahq.CandleListener;

class ParentCandleListener extends CandleListener {
    private final CtpMarketSpi  spi;

    ParentCandleListener(CtpMarketSpi spi) {
        this.spi = spi;
    }

    @Override
    public synchronized void onCandle(Candle candle, int fewMinutes, MarketSource source) {
        spi.callCandle(candle, fewMinutes, source);
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        spi.callError(throwable);
    }
}
