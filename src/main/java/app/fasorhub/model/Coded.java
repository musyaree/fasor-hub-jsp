package app.fasorhub.model;

// Enums that are stored in the database as a single character code
public interface Coded {
    String code();

    // Lets JSP expressions read the code as a property
    default String getCode() {
        return code();
    }

    static <E extends Enum<E> & Coded> E fromCode(Class<E> type, String code) {
        for (E value : type.getEnumConstants()) {
            if (value.code().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown code '" + code + "' for " + type.getSimpleName());
    }
}
