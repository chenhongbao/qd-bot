package org.quantdirect.bot.trader;

public abstract class TradeListener {
    public void onOrder(Order order) {}

    public void onTrade(Trade trade) {}
}
