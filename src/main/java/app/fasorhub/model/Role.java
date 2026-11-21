package app.fasorhub.model;

// What a person is allowed to do
public enum Role implements Coded {
    BORROWER("1", "Borrower"),
    MANAGER("2", "Manager");

    private final String code;
    private final String label;

    Role(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public String code() {
        return code;
    }

    public String getLabel() {
        return app.fasorhub.util.Messages.get("role." + code);
    }

    public boolean isManager() {
        return this == MANAGER;
    }

    public static Role of(String code) {
        return Coded.fromCode(Role.class, code);
    }
}
