package app.fasorhub.util;

import java.util.Locale;
import java.util.ResourceBundle;

// All user facing text comes from the message file and never from code
public final class Messages {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", Locale.ENGLISH);

    private Messages() {
    }

    public static String get(String key) {
        return BUNDLE.getString(key);
    }
}
