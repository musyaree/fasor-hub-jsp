package app.fasorhub.model;

import java.io.Serializable;

// A person who can log in and the password hash is removed before keeping it in the session
public record User(long id, String name, String email, String passwordHash, Role role, String identity, String phone)
        implements Serializable {

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getIdentity() {
        return identity;
    }

    public String getPhone() {
        return phone;
    }

    public User withoutPassword() {
        return new User(id, name, email, null, role, identity, phone);
    }
}
