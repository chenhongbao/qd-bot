package org.quantdirect.bot.trader;

import org.ctp4j.*;
import org.quantdirect.bot.tool.TOOLS;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CtpTraderSpi extends CThostFtdcTraderSpi {

    private final Map<String, TradeListener> listeners;
    private final CtpTrader t;
    private final Lock lck;
    private final Condition cond;
    private final ExecutorService es;
    private boolean valid;
    private int ref;

    CtpTraderSpi(CtpTrader trader) {
        valid = false;
        t = trader;
        ref = 0;
        lck = new ReentrantLock();
        cond = lck.newCondition();
        listeners = new ConcurrentHashMap<>();
        es = Executors.newCachedThreadPool();
    }

    String nextReference(TradeListener listener) {
        var r = Integer.toString(++ref);
        listeners.put(r, listener);
        return r;
    }


    private void callOrder(Order order) {
        var l = listeners.get(order.getReference());
        if (l != null) {
            es.submit(() -> {
                try {
                    l.onOrder(order);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                } finally {
                    tryClearListener(order);
                }
            });
        } else {
            TOOLS.log("No listener for order " + order.getReference() + ".", this);
        }
    }

    private void tryClearListener(Order order) {
        if (CtpTraderFlags.isOrderCompleted(order)) {
            listeners.remove(order.getReference());
        }
    }

    private Order createCommonOrder(CThostFtdcOrderField pOrder) {
        Order o = new Order();
        o.setReference(pOrder.getOrderRef());
        o.setDirection(CtpTraderFlags.stringifyDirection(pOrder.getDirection()));
        o.setOffset(CtpTraderFlags.stringifyOffset(pOrder.getCombOffsetFlag().charAt(0)));
        o.setPrice(pOrder.getLimitPrice());
        o.setQuantity(pOrder.getVolumeTotalOriginal());
        o.setTradedQuantity(pOrder.getVolumeTraded());
        o.setStatus(CtpTraderFlags.stringifyOrderStatus(pOrder.getOrderStatus()));
        o.setStatusMessage(pOrder.getStatusMsg());
        o.setTradingDay(TOOLS.toDay(pOrder.getTradingDay()));
        o.setUpdateTime(TOOLS.toTimeStamp(pOrder.getInsertDate(), pOrder.getUpdateTime(), 0));
        return o;
    }

    private Order createCommonOrder(CThostFtdcInputOrderField pInputOrder,
            CThostFtdcRspInfoField pRspInfo) {
        Order o = new Order();
        o.setReference(pInputOrder.getOrderRef());
        o.setReference(pInputOrder.getOrderRef());
        o.setDirection(CtpTraderFlags.stringifyDirection(pInputOrder.getDirection()));
        o.setOffset(CtpTraderFlags.stringifyOffset(pInputOrder.getCombOffsetFlag().charAt(0)));
        o.setPrice(pInputOrder.getLimitPrice());
        o.setQuantity(pInputOrder.getVolumeTotalOriginal());
        o.setTradedQuantity(0);
        o.setStatus(CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_Canceled));
        o.setStatusMessage(pRspInfo.getErrorMsg() + "(" + pRspInfo.getErrorID() + ").");
        o.setTradingDay(t.getTradingDay());
        o.setUpdateTime(LocalDateTime.now());
        return o;
    }

    private Trade createCommonTrade(CThostFtdcTradeField pTrade) {
        var t = new Trade();
        t.setReference(pTrade.getOrderRef());
        t.setDirection(CtpTraderFlags.stringifyDirection(pTrade.getDirection()));
        t.setOffset(CtpTraderFlags.stringifyOffset(pTrade.getOffsetFlag()));
        t.setPrice(pTrade.getPrice());
        t.setQuantity(pTrade.getVolume());
        t.setTradingDay(TOOLS.toDay(pTrade.getTradingDay()));
        t.setUpdateTime(TOOLS.toTimeStamp(pTrade.getTradeDate(), pTrade.getTradeTime(), 0));
        t.setTradeId(toTradeId(t.getUpdateTime()));
        return t;
    }

    private String toTradeId(LocalDateTime updateTime) {
        return Long.toString(updateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
    }

    private void callTrade(Trade trade) {
        var l = listeners.get(trade.getReference());
        if (l != null) {
            es.submit(() -> {
                try {
                    l.onTrade(trade);
                } catch (Throwable throwable) {
                    TOOLS.log(throwable, this);
                }
            });
        } else {
            TOOLS.log("No listener for order " + trade.getReference() + ".", this);
        }
    }

    @Override
    public void OnFrontConnected() {
        t.authenticate();
    }

    @Override
    public void OnFrontDisconnected(int nReason) {
        setAvailability(false);
        t.setTradingDay(null);
    }

    @Override
    public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Authentication failed(" + pRspInfo.getErrorID() + "), " +
                      pRspInfo.getErrorMsg() + ".", this);
        } else {
            t.login();
        }
    }

    @Override
    public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Login failed(" + pRspInfo.getErrorID() + "), " +
                      pRspInfo.getErrorMsg() + ".", this);
        } else {
            try {
                ref = Integer.parseInt(pRspUserLogin.getMaxOrderRef());
            } catch (NumberFormatException e) {
                TOOLS.log(e, this);
            }
            t.setTradingDay(TOOLS.toDay(pRspUserLogin.getTradingDay()));
            t.confirmSettlement();
        }
    }

    @Override
    public void OnRspSettlementInfoConfirm(
            CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Confirm settlement failed(" + pRspInfo.getErrorID() + "), " +
                      pRspInfo.getErrorMsg() + ".", this);
        } else {
            setAvailability(true);
        }
    }

    @Override
    public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            TOOLS.log("Logout failed." + pRspInfo.getErrorMsg() + "(" + pRspInfo.getErrorID() + ").", this);
        } else {
            setAvailability(false);
        }
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID,
            boolean bIsLast) {
        if (pRspInfo != null) {
            TOOLS.log("Error." + pRspInfo.getErrorMsg() + "(" + pRspInfo.getErrorID() + ").", this);
        }
        setAvailability(false);
    }

    @Override
    public void OnRtnOrder(CThostFtdcOrderField pOrder) {
        callOrder(createCommonOrder(pOrder));
    }

    @Override
    public void OnRtnTrade(CThostFtdcTradeField pTrade) {
        callTrade(createCommonTrade(pTrade));
    }


    private void printError(CThostFtdcRspInfoField pRspInfo) {
        if (pRspInfo != null) {
            TOOLS.log("Order failed. " + pRspInfo.getErrorMsg() + "(" + pRspInfo.getErrorID() + ").",
                    this);
        }
    }

    @Override
    public void OnErrRtnOrderInsert(CThostFtdcInputOrderField pInputOrder,
            CThostFtdcRspInfoField pRspInfo) {
        printError(pRspInfo);
        callOrder(createCommonOrder(pInputOrder, pRspInfo));
    }

    @Override
    public void OnRspOrderInsert(CThostFtdcInputOrderField pInputOrder,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        printError(pRspInfo);
        callOrder(createCommonOrder(pInputOrder, pRspInfo));
    }

    boolean getAvailability() {
        return valid;
    }

    private void setAvailability(boolean b) {
        valid = b;
        if (valid) {
            wakeStartup();
        }
    }

    void joinStartup(int timeout, TimeUnit unit) throws TimeoutException {
        lck.lock();
        try {
            if (!cond.await(timeout, unit)) {
                throw new TimeoutException("Trader start timeout.");
            }
        } catch (InterruptedException e) {
            TOOLS.log(e, this);
        } finally {
            lck.unlock();
        }
    }

    private void wakeStartup() {
        lck.lock();
        try {
            cond.signal();
        } finally {
            lck.unlock();
        }
    }
}
