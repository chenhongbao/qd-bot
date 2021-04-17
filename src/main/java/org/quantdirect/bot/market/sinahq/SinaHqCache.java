package org.quantdirect.bot.market.sinahq;

import com.google.gson.reflect.TypeToken;
import org.quantdirect.bot.market.Candle;
import org.quantdirect.bot.tool.TOOLS;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class SinaHqCache extends HqReader {

    private final Path root;

    SinaHqCache() {
        root = Paths.get("cache/");
        createDir(root);
    }

    private void createDir(Path d) {
        if (!Files.exists(d) || !Files.isDirectory(d)) {
            try {
                Files.createDirectories(d);
            } catch (IOException e) {
                throw new Error("Can't create directory.", e);
            }
        }
    }

    @Override
    public List<Candle> read(String instrumentId) {
        var f = Paths.get(root.toString(), instrumentId + ".day");
        return readJson(f);
    }

    @Override
    public List<Candle> read(String instrumentId, int fewMinutes) {
        var f = Paths.get(root.toString(), instrumentId + "." + fewMinutes);
        return readJson(f);
    }

    private List<Candle> readJson(Path f) {
        try {
            return TOOLS.json().from(f.toFile(), new TypeToken<List<Candle>>() {
            }.getType());
        } catch (IOException e) {
            throw new Error("Can't read cache file: " + f + ".", e);
        }
    }

    void write(List<Candle> candles, String instrumentId) {
        var f = Paths.get(root.toString(), instrumentId + ".day");
        writeJson(candles, f);
    }

    void write(List<Candle> candles, String instrumentId, int fewMinutes) {
        var f = Paths.get(root.toString(), instrumentId + "." + fewMinutes);
        writeJson(candles, f);
    }

    private void writeJson(List<Candle> candles, Path f) {
        ensureFile(f);
        write(candles, f);
    }

    private void ensureFile(Path f) {
        if (!Files.exists(f)) {
            try {
                Files.createFile(f);
            } catch (IOException e) {
                throw new Error("Can't create cache file: " + f + ".");
            }
        }
    }

    private void write(List<Candle> candles, Path f) {
        try (FileWriter fw = new FileWriter(f.toFile(), false)) {
            var str = TOOLS.json().to(candles);
            fw.write(str);
        } catch (IOException e) {
            throw new Error("Can't write cache file: " + f + ".", e);
        }
    }
}
