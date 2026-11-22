package app.fasorhub.exception;

/**
 * Thrown when user input fails validation.
 * The message key refers to messages_en.properties (e.g., "p21", "p22", "p23").
 */
public class ValidationException extends Exception {
    private final String messageKey;

    public ValidationException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
