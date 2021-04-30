package org.quantdirect.bot.trader;

class CtpTraderFlags {
    static final char THOST_FTDC_CC_Immediately = '1';
    static final char THOST_FTDC_FCC_NotForceClose = '0';
    static final char THOST_FTDC_HF_Speculation = '1';
    static final char THOST_FTDC_OPT_LimitPrice = '2';
    static final char THOST_FTDC_TC_GFD = '3';
    static final char THOST_FTDC_VC_AV = '1';
    static final char THOST_FTDC_D_Buy = '0';
    static final char THOST_FTDC_D_Sell = '1';
    static final char THOST_FTDC_OF_Open = '0';
    static final char THOST_FTDC_OF_Close = '1';
    static final char THOST_FTDC_OF_ForceClose = '2';
    static final char THOST_FTDC_OF_CloseToday = '3';
    static final char THOST_FTDC_OF_CloseYesterday = '4';
    static final char THOST_FTDC_OF_ForceOff = '5';
    static final char THOST_FTDC_OF_LocalForceClose = '6';
    static final char THOST_FTDC_OST_AllTraded = '0';
    static final char THOST_FTDC_OST_PartTradedQueueing = '1';
    static final char THOST_FTDC_OST_PartTradedNotQueueing = '2';
    static final char THOST_FTDC_OST_NoTradeQueueing = '3';
    static final char THOST_FTDC_OST_NoTradeNotQueueing = '4';
    static final char THOST_FTDC_OST_Canceled = '5';
    static final char THOST_FTDC_OST_Unknown ='a';
    static final char THOST_FTDC_OST_NotTouched = 'b';
    static final char THOST_FTDC_OST_Touched = 'c';

    static String stringifyDirection(char direction) {
        switch (direction) {
            case THOST_FTDC_D_Buy:
                return "Buy";
            case THOST_FTDC_D_Sell:
                return "Sell";
            default:
                throw new Error("Illegal direction: " + direction + ".");
        }
    }

    static String stringifyOffset(char offset) {
        switch (offset) {
            case THOST_FTDC_OF_Open:
                return "Open";
            case THOST_FTDC_OF_Close:
                return "Close";
            case THOST_FTDC_OF_ForceClose:
                return "ForceClose";
            case THOST_FTDC_OF_CloseToday:
                return "CloseToday";
            case THOST_FTDC_OF_CloseYesterday:
                return "CloseYesterday";
            case THOST_FTDC_OF_ForceOff:
                return "ForceOff";
            case THOST_FTDC_OF_LocalForceClose:
                return "LocalForceClose";
            default:
                throw new Error("Illegal offset: " + offset + ".");
        }
    }

    static String stringifyOrderStatus(char status) {
        switch (status) {
            case THOST_FTDC_OST_AllTraded:
                return "AllTraded";
            case THOST_FTDC_OST_PartTradedQueueing:
                return "PartTradedQueueing";
            case THOST_FTDC_OST_PartTradedNotQueueing:
                return "PartTradedNotQueueing";
            case THOST_FTDC_OST_NoTradeQueueing:
                return "NoTradeQueueing";
            case THOST_FTDC_OST_NoTradeNotQueueing:
                return "NoTradeNotQueueing";
            case THOST_FTDC_OST_Canceled:
                return "Canceled";
            case THOST_FTDC_OST_Unknown:
                return "Unknown";
            case THOST_FTDC_OST_NotTouched:
                return "NotTouched";
            case THOST_FTDC_OST_Touched:
                return "Touched";
            default:
                throw new Error("Illegal order status: " + status + ".");
        }
    }

    static boolean isOrderCompleted(Order order) {
        var a = CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_AllTraded);
        var b = CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_PartTradedNotQueueing);
        var c = CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_NoTradeNotQueueing);
        var d = CtpTraderFlags.stringifyOrderStatus(CtpTraderFlags.THOST_FTDC_OST_Canceled);
        var s = order.getStatus();
        return s.equals(a) || s.equals(b) || s.equals(c) || s.equals(d);
    }
}
