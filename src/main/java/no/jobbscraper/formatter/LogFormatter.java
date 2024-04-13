package no.jobbscraper.formatter;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private final Date date = new Date();

    @Override
    public String format(LogRecord record) {
        String[] classNameArr = record.getSourceClassName().split("\\.");
        String className = classNameArr[classNameArr.length-1];
        date.setTime(record.getMillis());

        return String.format("[%1$s] [%2$s] [%3$s] %4$s\n",
                date, className, record.getLevel().getName(), formatMessage(record));
    }
}
