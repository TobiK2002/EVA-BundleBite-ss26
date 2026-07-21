package Core.Models;

import java.time.LocalDate;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private String email;
    private String adress;

    public User(UUID id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.adress = address;
    }


    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAdress() {
        return adress;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getId() == user.getId();
    }

}

