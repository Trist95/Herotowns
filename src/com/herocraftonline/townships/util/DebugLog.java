package com.herocraftonline.townships.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.*;

public class DebugLog {

    private FileHandler fh;

    private final Logger log;

    public DebugLog(String logger, String file) {
        log = Logger.getLogger(logger);

        try {
            fh = new FileHandler(file, true);
            log.setUseParentHandlers(false);
            for (final Handler handler : log.getHandlers()) {
                log.removeHandler(handler);
            }
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            fh.setFormatter(new LogFormatter());
        } catch (final SecurityException|IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        fh.close();
    }

    public void log(Level level, String msg) {
        log.log(level, msg);
    }

    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        log.throwing(sourceClass, sourceMethod, thrown);
    }

    public Logger getLogger() {
        return log;
    }

    private class LogFormatter extends Formatter {

        private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (ex != null) {
                final StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }
}
