package org.quantdirect.bot.trader;

import org.ctp4j.*;
import org.quantdirect.bot.tool.CtpTraderConfiguration;
import org.quantdirect.bot.tool.TOOLS;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CtpTrader extends Trader {
    private final CtpTraderConfiguration cfg;
    private final CThostFtdcTraderApi api;
    private final CtpTraderSpi spi;
    private final Timer tmr;
    private LocalDate tradingDay;

    CtpTrader(String flowPath) throws IOException {
        TOOLS.mkdir(flowPath);
        cfg = TOOLS.json().from(new File("trader.json"), CtpTraderConfiguration.class);
        api = CThostFtdcTraderApi.CreateFtdcTraderApi(flowPath);
        spi = new CtpTraderSpi(this);
        tmr = new Timer();
        refresh();
    }

    @Override
    public void buyOpen(String instrumentId, String exchangeId, double price, int quantity) {
        var x = new SyncTradeListener();
        buyOpen(instrumentId, exchangeId, price, quantity, x);
        x.join();
    }

    @Override
    public void buyOpen(String instrumentId, String exchangeId, double price, int quantity, TradeListener listener) {
        ensureAvailability();
        var c = createCommonOrder(instrumentId, exchangeId, price, quantity, listener);
        c.setCombOffsetFlag(String.valueOf(CtpTraderFlags.THOST_FTDC_OF_Open));
        c.setDirection(CtpTraderFlags.THOST_FTDC_D_Buy);
        var r = api.ReqOrderInsert(c, c.getRequestID());
        if (r != 0) {
            TOOLS.log("Order insert fail(" + r + ").", this);
            throw new Error("Order insert fail(" + r + ").");
        }
    }

    @Override
    public void buyClose(String instrumentId, String exchangeId, double price, int quantity, boolean today) {
        var x = new SyncTradeListener();
        buyClose(instrumentId, exchangeId, price, quantity, today, x);
        x.join();
    }

    @Override
    public void buyClose(String instrumentId, String exchangeId, double price, int quantity, boolean today, TradeListener listener) {
        ensureAvailability();
        var c = createCommonOrder(instrumentId, exchangeId, price, quantity, listener);
        c.setCombOffsetFlag(closeOffset(today));
        c.setDirection(CtpTraderFlags.THOST_FTDC_D_Buy);
        var r = api.ReqOrderInsert(c, c.getRequestID());
        if (r != 0) {
            TOOLS.log("Order insert fail(" + r + ").", this);
            throw new Error("Order insert fail(" + r + ").");
        }
    }

    @Override
    public void sellOpen(String instrumentId, String exchangeId, double price, int quantity) {
        var x = new SyncTradeListener();
        sellOpen(instrumentId, exchangeId, price, quantity, x);
        x.join();
    }

    @Override
    public void sellOpen(String instrumentId, String exchangeId, double price, int quantity, TradeListener listener) {
        ensureAvailability();
        var c = createCommonOrder(instrumentId, exchangeId, price, quantity, listener);
        c.setCombOffsetFlag(String.valueOf(CtpTraderFlags.THOST_FTDC_OF_Open));
        c.setDirection(CtpTraderFlags.THOST_FTDC_D_Sell);
        var r = api.ReqOrderInsert(c, c.getRequestID());
        if (r != 0) {
            TOOLS.log("Order insert fail(" + r + ").", this);
            throw new Error("Order insert fail(" + r + ").");
        }
    }

    @Override
    public void sellClose(String instrumentId, String exchangeId, double price, int quantity, boolean today) {
        var x = new SyncTradeListener();
        sellClose(instrumentId, exchangeId, price, quantity, today, x);
        x.join();
    }

    @Override
    public void sellClose(String instrumentId, String exchangeId, double price, int quantity, boolean today, TradeListener listener) {
        ensureAvailability();
        var c = createCommonOrder(instrumentId, exchangeId, price, quantity, listener);
        c.setCombOffsetFlag(closeOffset(today));
        c.setDirection(CtpTraderFlags.THOST_FTDC_D_Sell);
        var r = api.ReqOrderInsert(c, c.getRequestID());
        if (r != 0) {
            TOOLS.log("Order insert fail(" + r + ").", this);
            throw new Error("Order insert fail(" + r + ").");
        }
    }

    @Override
    public boolean isAvailable() {
        return spi.isAvailable();
    }

    @Override
    public LocalDate getTradingDay() {
        return tradingDay;
    }

    void setTradingDay(LocalDate day) {
        tradingDay = day;
    }

    private void refresh() {
        startApi();
        startTimer();
    }

    private void startApi() {
        api.RegisterSpi(spi);
        api.SubscribePrivateTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
        api.SubscribePublicTopic(THOST_TE_RESUME_TYPE.THOST_TERT_QUICK);
        cfg.getFronts().forEach(addr -> {
            api.RegisterFront(addr);
        });
        api.Init();
        TOOLS.log(CThostFtdcTraderApi.GetApiVersion(), this);
    }

    private void startTimer() {
        final var ms = 15 * TimeUnit.SECONDS.toMillis(15);
        tmr.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    ensureAvailability();
                } catch (Throwable ignored) {
                }
            }
        }, ms / 3, ms);
    }

    private void ensureAvailability() {
        if (!spi.isConnected()) {
            throw new Error("Trader is not connected.");
        }
        if (!spi.isAvailable()) {
            authenticate();
            try {
                joinStartup();
            } catch (TimeoutException e) {
                throw new Error("Trader not available. " + e.getMessage(), e);
            }
        }
    }

    private void joinStartup() throws TimeoutException {
        spi.joinStartup(15, TimeUnit.SECONDS);
    }

    void authenticate() {
        var c = new CThostFtdcReqAuthenticateField();
        c.setBrokerID(cfg.getBrokerId());
        c.setUserID(cfg.getUserId());
        c.setUserProductInfo("C_SP_BOT");
        c.setAuthCode(cfg.getAuthCode());
        c.setAppID(cfg.getAppId());
        var r = api.ReqAuthenticate(c, TOOLS.nextRequestId());
        if (r != 0) {
            TOOLS.log("ReqAuthenticate returns " + r + ".", this);
        }
    }

    void login() {
        var c = new CThostFtdcReqUserLoginField();
        c.setBrokerID(cfg.getBrokerId());
        c.setUserID(cfg.getUserId());
        c.setPassword(cfg.getPassword());
        var r = api.ReqUserLogin(c, TOOLS.nextRequestId());
        if (r != 0) {
            TOOLS.log("ReqUserLogin returns " + r + ".", this);
        }
    }

    void confirmSettlement() {
        var c = new CThostFtdcSettlementInfoConfirmField();
        c.setBrokerID(cfg.getBrokerId());
        c.setInvestorID(cfg.getUserId());
        var r = api.ReqSettlementInfoConfirm(c, TOOLS.nextRequestId());
        if (r != 0) {
            TOOLS.log("ReqSettlementInfoConfirm returns " + r + ".", this);
        }
    }

    private String closeOffset(Boolean today) {
        if (today == null) {
            return String.valueOf(CtpTraderFlags.THOST_FTDC_OF_Close);
        }
        return today ? String.valueOf(CtpTraderFlags.THOST_FTDC_OF_CloseToday) :
               String.valueOf(CtpTraderFlags.THOST_FTDC_OF_CloseYesterday);
    }

    private CThostFtdcInputOrderField createCommonOrder(String instrumentId,
            String exchangeId, double price, int quantity, TradeListener listener) {
        CThostFtdcInputOrderField c = new CThostFtdcInputOrderField();
        c.setAccountID(cfg.getUserId());
        c.setBrokerID(cfg.getBrokerId());
        c.setBusinessUnit("");
        c.setClientID("");
        c.setCombHedgeFlag(String.valueOf(CtpTraderFlags.THOST_FTDC_HF_Speculation));
        c.setContingentCondition(CtpTraderFlags.THOST_FTDC_CC_Immediately);
        c.setCurrencyID("CNY");
        c.setForceCloseReason(CtpTraderFlags.THOST_FTDC_FCC_NotForceClose);
        c.setGTDDate("");
        c.setIPAddress("");
        c.setInvestUnitID("");
        c.setInvestorID(cfg.getUserId());
        c.setIsAutoSuspend(0);
        c.setIsSwapOrder(0);
        c.setMacAddress("");
        c.setMinVolume(1);
        c.setOrderPriceType(CtpTraderFlags.THOST_FTDC_OPT_LimitPrice);
        c.setOrderRef(spi.nextReference(listener));
        c.setRequestID(TOOLS.nextRequestId());
        c.setStopPrice(0);
        c.setTimeCondition(CtpTraderFlags.THOST_FTDC_TC_GFD);
        c.setUserForceClose(0);
        c.setUserID(cfg.getBrokerId());
        c.setVolumeCondition(CtpTraderFlags.THOST_FTDC_VC_AV);
        c.setExchangeID(exchangeId);
        c.setInstrumentID(instrumentId);
        c.setLimitPrice(price);
        c.setVolumeTotalOriginal(quantity);
        return c;
    }
}
