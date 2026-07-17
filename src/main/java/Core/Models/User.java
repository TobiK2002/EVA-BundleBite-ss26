package Core.Models;

import java.time.LocalDate;
import java.util.UUID;

public class User {

    private final UUID id;
    private String username;
    private String email;

    public User(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }


    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }



    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getId() == user.getId();
    }

}

