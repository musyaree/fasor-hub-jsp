package app.fasorhub.util;

// Wraps database failures so callers do not handle checked errors
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
