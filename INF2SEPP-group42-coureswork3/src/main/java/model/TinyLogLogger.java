package model;

import org.tinylog.Logger;
import org.tinylog.configuration.Configuration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Manages logging to a file
 */
public class TinyLogLogger {
    // Singleton instance
    private static TinyLogLogger instance;

    // Private constructor to prevent direct instantiation
    private TinyLogLogger() {
        // Configure TinyLog
        Configuration.set("writerFile", "system.log");
        Configuration.set("writer", "file");
        Configuration.set("writer.format", "[{date}] User: {user}, Action: {action}, Inputs: {inputs}, Status: {status}");
    }

    // Singleton getInstance method
    public static synchronized TinyLogLogger getInstance() {
        if (instance == null) {
            instance = new TinyLogLogger();
        }
        return instance;
    }

    // Date time formatter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs message in correct format using given parameters
     *
     * @param timestamp     timestamp of the log
     * @param userId        the id of the user doing the action that creates the log
     * @param actionName    the action that creates the log
     * @param inputs        the user inputs for the action
     * @param status        the status of the action
     */
    public static void log(long timestamp, String userId, String actionName, String inputs, String status) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        Logger.info("[{}] User: {}, Action: {}, Inputs: {}, Status: {}",
                dateTime.format(formatter), userId, actionName, inputs, status);
    }
}