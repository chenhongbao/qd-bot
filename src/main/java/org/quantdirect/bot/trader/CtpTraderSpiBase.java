package org.quantdirect.bot.trader;

import org.ctp4j.*;
import org.quantdirect.bot.tool.TOOLS;

public abstract class CtpTraderSpiBase extends CThostFtdcTraderSpi {
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
    public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callAuth(pRspAuthenticateField, pRspInfo, nRequestID, bIsLast);
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
    public void OnRspSettlementInfoConfirm(
            CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callSettlement(pSettlementInfoConfirm, pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callLogout(pUserLogout, pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callError(pRspInfo, nRequestID, bIsLast);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRtnOrder(CThostFtdcOrderField pOrder) {
        try {
            callOrder(pOrder);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRtnTrade(CThostFtdcTradeField pTrade) {
        try {
            callTrade(pTrade);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnErrRtnOrderInsert(CThostFtdcInputOrderField pInputOrder,
            CThostFtdcRspInfoField pRspInfo) {
        try {
            callRtnOrder(pInputOrder, pRspInfo);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    @Override
    public void OnRspOrderInsert(CThostFtdcInputOrderField pInputOrder,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        try {
            callRtnOrder(pInputOrder, pRspInfo);
        } catch (Throwable throwable) {
            TOOLS.log(throwable, this);
        }
    }

    protected abstract void callConnected();

    protected abstract void callDisconnected(int nReason);

    protected abstract void callAuth(CThostFtdcRspAuthenticateField pRspAuthenticateField,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callLogin(CThostFtdcRspUserLoginField pRspUserLogin,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callSettlement(CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callLogout(CThostFtdcUserLogoutField pUserLogout,
            CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callError(CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast);

    protected abstract void callOrder(CThostFtdcOrderField pOrder);

    protected abstract void callTrade(CThostFtdcTradeField pTrade);

    protected abstract void callRtnOrder(CThostFtdcInputOrderField pInputOrder, CThostFtdcRspInfoField pRspInfo);
}
