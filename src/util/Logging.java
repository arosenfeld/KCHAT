package util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logging {
    private static Logger logger = Logger.getLogger("KCHAT");
    private static Handler handler = new ConsoleHandler();

    static {
        handler.setFormatter(new LogFormatter());
        for (Handler iHandler : logger.getParent().getHandlers()) {
            logger.getParent().removeHandler(iHandler);
        }
        logger.addHandler(handler);
    }

    public static Logger getLogger() {
        return logger;
    }

    private static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(record.getLevel().toString());
            sb.append("] ");
            sb.append(record.getSourceClassName());
            sb.append(".");
            sb.append(record.getSourceMethodName());
            sb.append(": ");
            sb.append(record.getMessage());
            sb.append("\n");

            return sb.toString();
        }
    }
}
