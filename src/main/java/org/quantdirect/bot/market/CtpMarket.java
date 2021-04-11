package org.quantdirect.bot.market;

import org.ctp4j.*;
import org.quantdirect.bot.tool.CtpMarketConfiguration;
import org.quantdirect.bot.tool.TOOLS;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CtpMarket extends Market {
    private final CtpMarketConfiguration cfg;
    private final CThostFtdcMdApi api;
    private final CtpMarketSpi spi;
    private LocalDate tradingDay;

    CtpMarket(String flowPath, boolean udp, boolean multicast, MarketListener listener, String[] args) throws TimeoutException, IOException {
        TOOLS.mkdir(flowPath);
        cfg = TOOLS.json().from(new File("market.json"), CtpMarketConfiguration.class);
        api = CThostFtdcMdApi.CreateFtdcMdApi(flowPath, udp, multicast);
        spi = new CtpMarketSpi(this, args);
        spi.addListener(listener);
        spi.callInit();
        refresh();
    }

    private void refresh() throws TimeoutException {
        api.RegisterSpi(spi);
        cfg.getFronts().forEach(addr -> {
            api.RegisterFront(addr);
        });
        api.Init();
        spi.joinStartup(15, TimeUnit.SECONDS);
        TOOLS.log(CThostFtdcMdApi.GetApiVersion(), this);
    }

    void login() {
        sendLogin();
    }

    void setTradingDay(LocalDate day) {
        tradingDay = day;
    }

    private void sendLogin() {
        var c = new CThostFtdcReqUserLoginField();
        c.setBrokerID(cfg.getBrokerId());
        c.setUserID(cfg.getUserId());
        c.setPassword(cfg.getPassword());
        var r = api.ReqUserLogin(c, TOOLS.nextRequestId());
        if (r != 0) {
            TOOLS.log("ReqUserLogin returns " + r + ".", this);
        }
    }

    @Override
    public void subscribe(String... instrumentId) {
        var r = api.SubscribeMarketData(instrumentId, instrumentId.length);
        if (r != 0) {
            TOOLS.log("Subscription returns " + r + ".", this);
            throw new Error("Subscription returns " + r + ".");
        }
    }

    @Override
    public void join() {
        while (true) {
            try {
                (new CountDownLatch(1)).await();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public LocalDate getTradingDay() {
        return tradingDay;
    }

    @Override
    public void addListener(MarketListener listener) {
        spi.addListener(listener);
    }

}
