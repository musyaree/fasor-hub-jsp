package app.fasorhub.exception;

import app.fasorhub.util.Messages;

// Thrown when a requested slot can no longer be booked
public class SlotUnavailableException extends RuntimeException {
    private final String messageKey;

    public SlotUnavailableException(String messageKey) {
        super(Messages.get(messageKey));
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
