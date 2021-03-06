package org.quantdirect.bot.market;

import org.ctp4j.*;
import org.quantdirect.bot.tool.TOOLS;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CtpMarketSpi extends CtpMarketSpiBase {
    private final CtpMarket m;
    private final Queue<MarketListener> ml;
    private final Lock lck;
    private final Lock subsLck;
    private final Condition cond;
    private final Condition subsCond;
    private final String[] args;
    private final ExecutorService es;
    private final Set<String> subs;

    private boolean hasError;

    CtpMarketSpi(CtpMarket market, String[] arguments) {
        args = arguments;
        m = market;
        ml = new ConcurrentLinkedQueue<>();
        lck = new ReentrantLock();
        subsLck = new ReentrantLock();
        cond = lck.newCondition();
        subsCond = subsLck.newCondition();
        es = Executors.newCachedThreadPool();
        subs = new ConcurrentSkipListSet<>();
        hasError = false;
    }

    void addListener(MarketListener listener) {
        if (listener != null) {
            ml.add(listener);
        }
    }

    void removeSubscribed(String instrumentId) {
        subs.remove(instrumentId);
    }

    @SuppressWarnings("unused")
    void callCandle(int fewMinutes, MarketSource source, Candle... candles) {
        if (candles.length == 0) {
            return;
        }
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                for (var index = 0; index < candles.length; ++index) {
                    try {
                        l.onCandle(fewMinutes, source, candles[index], index == candles.length - 1);
                    } catch (Throwable throwable) {
                        TOOLS.log(throwable, this);
                        try {
                            l.onError(throwable);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        });
    }

    void callInit() {
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                try {
                    l.onInit(args);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                    try {
                        l.onError(throwable);
                    } catch (Throwable ignored) {
                    }
                }
            });
        });
    }

    private void callLogin(Market market) {
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                try {
                    l.onLogin(market);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                    try {
                        l.onError(throwable);
                    } catch (Throwable ignored) {
                    }
                }
            });
        });
    }

    private void callDisconnect(int reason) {
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                try {
                    l.onDisconnected(reason);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                    try {
                        l.onError(throwable);
                    } catch (Throwable ignored) {
                    }
                }
            });
        });
    }

    private void callTick(Tick tick) {
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                try {
                    l.onTick(tick);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                    try {
                        l.onError(throwable);
                    } catch (Throwable ignored) {
                    }
                }
            });
        });
    }

    private void callError(int errorId, String errorMsg) {
        callError(new Error("[" + errorId + "]" + errorMsg));
    }

    void callError(Throwable throwable) {
        es.submit(() -> {
            ml.stream().parallel().forEach(l -> {
                try {
                    l.onError(throwable);
                } catch (Throwable ex) {
                    TOOLS.log(ex, this);
                    try {
                        l.onError(ex);
                    } catch (Throwable ignored) {
                    }
                }
            });
        });
    }

    @Override
    public void callConnected() {
        m.login();
    }

    @Override
    public void callDisconnected(int nReason) {
        TOOLS.log("Market disconnected " + nReason + ".", this);
        m.setTradingDay(null);
        callDisconnect(nReason);
    }

    @Override
    public void callLogin(CThostFtdcRspUserLoginField pRspUserLogin,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Login failed(" + pRspInfo.getErrorID() + "), " +
                      pRspInfo.getErrorMsg() + ".", this);
            callError(pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
        } else {
            m.setTradingDay(TOOLS.toDay(pRspUserLogin.getTradingDay()));
            callLogin(m);
        }
    }

    @Override
    public void callError(CThostFtdcRspInfoField pRspInfo, int nRequestID,
            boolean bIsLast) {
        TOOLS.log("Error(" + pRspInfo.getErrorID() + "), " +
                  pRspInfo.getErrorMsg() + ".", this);
        callError(pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
    }

    @Override
    public void callSubMd(CThostFtdcSpecificInstrumentField pSpecificInstrument,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Subscription failed(" + pRspInfo.getErrorID() + "), " +
                      pRspInfo.getErrorMsg() + ".", this);
            callError(pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
            hasError = true;
            wakeSubscription("");
        } else {
            wakeSubscription(pSpecificInstrument.getInstrumentID());
        }
    }

    @Override
    public void callMd(CThostFtdcDepthMarketDataField pDepthMarketData) {
        callTick(toTick(pDepthMarketData));
    }

    private Tick toTick(CThostFtdcDepthMarketDataField pDepthMarketData) {
        var t = new Tick();
        t.setLastPrice(pDepthMarketData.getLastPrice());
        t.setAskPrice(pDepthMarketData.getAskPrice1());
        t.setAskVolume(pDepthMarketData.getAskVolume1());
        t.setBidPrice(pDepthMarketData.getBidPrice1());
        t.setBidVolume(pDepthMarketData.getBidVolume1());
        t.setOpenPrice(pDepthMarketData.getOpenPrice());
        t.setHighPrice(pDepthMarketData.getHighestPrice());
        t.setLowPrice(pDepthMarketData.getLowestPrice());
        t.setClosePrice(pDepthMarketData.getClosePrice());
        t.setPreClosePrice(pDepthMarketData.getPreClosePrice());
        t.setSettlePrice(pDepthMarketData.getSettlementPrice());
        t.setPreSettlePrice(pDepthMarketData.getPreSettlementPrice());
        t.setUpperLimitPrice(pDepthMarketData.getUpperLimitPrice());
        t.setLowerLimitPrice(pDepthMarketData.getLowerLimitPrice());
        t.setAveragePrice(pDepthMarketData.getAveragePrice());
        t.setExchangeId(pDepthMarketData.getExchangeID());
        t.setInstrumentId(pDepthMarketData.getInstrumentID());
        t.setTotalVolume(pDepthMarketData.getVolume());
        t.setOpenInterest((long) pDepthMarketData.getOpenInterest());
        t.setPreOpenInterest((long) pDepthMarketData.getPreOpenInterest());
        t.setTradingDay(TOOLS.toDay(pDepthMarketData.getTradingDay()));
        t.setActionDay(TOOLS.toDay(pDepthMarketData.getActionDay()));
        t.setUpdateTime(TOOLS.toTime(pDepthMarketData.getUpdateTime(), pDepthMarketData.getUpdateMillisec()));
        /*
         * Because action day from different exchanges has different meanings, that
         * some are natural day while the others are trading day (next day if it is night
         * session), here set it to the current time stamp so the ticks can be sorted
         * to time line order.
         */
        t.setTimeStamp(LocalDateTime.now());
        t.setTickId(toTickId(t, t.getTimeStamp()));
        return t;
    }

    private String toTickId(Tick self, LocalDateTime updateTime) {
        return Long.toString(updateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()) + self.hashCode();
    }

    void joinSubscription(String instrumentId) {
        subsLck.lock();
        try {
            while (!subs.contains(instrumentId) && !hasError) {
                try {
                    subsCond.await();
                } catch (InterruptedException e) {
                    TOOLS.log(e, this);
                }
            }
        } finally {
            subsLck.unlock();
            hasError = false;
        }
    }

    void wakeSubscription(String instrumentId) {
        subs.add(instrumentId);
        subsLck.lock();
        try {
            subsCond.signalAll();
        } finally {
            subsLck.unlock();
        }
    }
}
