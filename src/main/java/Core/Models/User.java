package Core.Models;

import java.time.LocalDate;
import java.util.UUID;

public class User {

    private final UUID id;
    private String name;
    private String email;
    private Address address;

    public User(UUID id, String name, String email, Address address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
    }

    public User(UUID id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = Address.fromString(address);
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

    public Address getAddress() {
        return address;
    }

    public String getAddressAsString() {
        return address.asSingleLine();
    }

    public boolean livesInSamePostalCodeAs(User other) {
        return other != null && address.hasSamePostalCode(other.address);
    }

    public boolean livesInSameCityAs(User other) {
        return other != null && address.hasSameCity(other.address);
    }

    public boolean canCreateGroupOrderWith(User other) {
        return other != null && address.isInSameArea(other.address);
    }

    public void setAddress(String address) {
        this.address = Address.fromString(address);
    }



    public void setName(String name) {
        this.name = name;
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

