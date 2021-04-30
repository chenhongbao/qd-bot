package org.quantdirect.bot.market;

import org.ctp4j.*;
import org.quantdirect.bot.tool.TOOLS;

public abstract class CtpMarketSpiBase extends CThostFtdcMdSpi  {
    @Override
    public void OnFrontConnected() {
        try {
            callConnected();
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnFrontDisconnected(int nReason) {
        try {
            callDisconnected(nReason);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callLogin(pRspUserLogin, pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID,
            boolean bIsLast) {
        try {
            callError(pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspSubMarketData(CThostFtdcSpecificInstrumentField pSpecificInstrument,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callSubMd(pSpecificInstrument, pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
        try {
            callMd(pDepthMarketData);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    protected abstract void callConnected();

    protected abstract void callDisconnected(int nReason);

    protected abstract void callLogin(CThostFtdcRspUserLoginField pRspUserLogin,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callSubMd(CThostFtdcSpecificInstrumentField pSpecificInstrument,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callMd(CThostFtdcDepthMarketDataField pDepthMarketData);
}
