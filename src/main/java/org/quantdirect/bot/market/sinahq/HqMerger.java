package org.quantdirect.bot.market.sinahq;

import org.quantdirect.bot.market.Candle;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

class HqMerger {
    private final List<Candle> cache;

    HqMerger() {
        cache = new LinkedList<>();
    }

    List<Candle> merge(List<Candle> update) {
        if (update == null || update.isEmpty()) {
            return new LinkedList<>();
        }
        update.sort(Comparator.comparing(Candle::getTime));
        if (cache.isEmpty()) {
            cache.addAll(update);
            return new LinkedList<>(update);
        } else {
            var r = new LinkedList<Candle>();
            var last = cache.get(cache.size() - 1);
            for (var i = update.size() - 1; i >= 0; --i) {
                var n = update.get(i);
                if (n.getTime().isAfter(last.getTime())) {
                    r.addFirst(n);
                } else {
                    break;
                }
            }
            cache.addAll(r);
            return r;
        }
    }

    List<Candle> getCache() {
        return cache;
    }
}
