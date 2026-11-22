package app.fasorhub.exception;

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
