package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.AreaShop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignErrorLogger {

    public static final String PREFIX_WARN = "[Warning] [Sign Feature] ";
    public static final DateFormat FORMAT = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    private final List<String> cache = new LinkedList<>();
    private final AtomicBoolean needsSaving = new AtomicBoolean(false);

    private final File file;

    public SignErrorLogger(File file) {
        this.file = Objects.requireNonNull(file);
    }

    public void clearCache() {
        synchronized (this.cache) {
            this.cache.clear();
        }
    }

    public SignErrorLogger submitWarning(String message) {
        final String actual = FORMAT.format(Date.from(Instant.now())) + PREFIX_WARN + message;
        synchronized (this.cache) {
            this.cache.add(actual);
        }
        return this;
    }

    public void queueSave() {
        this.needsSaving.set(true);
    }

    public void saveIfDirty() {
        if (this.needsSaving.get()) {
            save();
        }
    }

    public synchronized void save() {
        AreaShop.debugTask("[Sign Feature] Dumping sign errors to disk...");
        final String[] copy;
        synchronized (this.cache) {
            copy = cache.toArray(new String[0]);
        }
        final StringJoiner joiner = new StringJoiner(System.lineSeparator());
        for (String s : copy) {
            joiner.add(s);
        }
        try (OutputStream os = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(joiner.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            AreaShop.error("Failed to update the sign error log!");
        } finally {
            AreaShop.debugTask("[Sign Feature] Error dump complete.");
        }
    }
}
