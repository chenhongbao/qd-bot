package org.quantdirect.bot.trader;

import org.quantdirect.bot.trader.Order;
import org.quantdirect.bot.trader.Trade;

public interface TradeListener {
    void onOrder(Order order);

    void onTrade(Trade trade);
}
