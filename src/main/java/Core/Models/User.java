package Core.Models;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class User {

    private final String email;
    private String name;
    private Address address;

    public User( String name, String email, Address address) {

        this.email = email;
        this.name = name;
        this.address = address;
    }

    public User(String name, String email, String address) {
        this.name = name;
        this.email = email;
        this.address = Address.fromString(address);
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = Address.fromString(address);
    }

    public void setName(String name) {
        this.name = name;
    }

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getEmail(), user.getEmail());
    }

}

