package app.fasorhub.exception;

import app.fasorhub.util.Messages;

// Thrown when the booking changed status before the action was processed
public class BookingStatusChangedException extends RuntimeException {
    private final String messageKey;

    public BookingStatusChangedException(String messageKey) {
        super(Messages.get(messageKey));
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
