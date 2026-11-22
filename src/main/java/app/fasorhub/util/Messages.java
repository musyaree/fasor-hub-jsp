package app.fasorhub.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

// All user facing text comes from the message file and never from code
public final class Messages {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", Locale.ENGLISH);

    private static final Map<String, String> ALL = load();

    private Messages() {
    }

    public static String get(String key) {
        return BUNDLE.getString(key);
    }

    public static Map<String, String> all() {
        return ALL;
    }

    private static Map<String, String> load() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : BUNDLE.keySet()) {
            map.put(key, BUNDLE.getString(key));
        }
        return Collections.unmodifiableMap(map);
    }
}
