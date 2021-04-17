package org.quantdirect.bot.trader;

import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SyncTradeListener extends TradeListener {

    private final Stack<Order> orders;
    private final Lock lock;
    private final Condition cond;

    SyncTradeListener() {
        orders = new Stack<>();
        lock = new ReentrantLock();
        cond = lock.newCondition();
    }

    void join() {
        lock.lock();
        try {
            while (orders.isEmpty() || !CtpTraderFlags.isOrderCompleted(orders.peek())) {
                try {
                    cond.await();
                } catch (InterruptedException e) {
                }
            }
        } finally {
            lock.unlock();
            checkOrder();
        }
    }

    @Override
    public void onOrder(Order order) {
        orders.add(order);
        tryWake(order);
    }

    @Override
    public void onTrade(Trade trade) {

    }

    private void wake() {
        lock.lock();
        try {
            cond.signal();
        } finally {
            lock.unlock();
        }
    }

    private void tryWake(Order order) {
        if (CtpTraderFlags.isOrderCompleted(order)) {
            wake();
        }
    }

    private void checkOrder() {
        var current = orders.peek();
        var a = CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_AllTraded);
        if (!current.getStatus().equals(a)) {
            throw new Error("Order not completed. " + a + ".");
        }
    }
}
