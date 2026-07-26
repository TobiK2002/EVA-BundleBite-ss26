package Core.Models;


import java.util.UUID;

public class Restaurant {
    private final UUID id;
    private String name;
    private Address address;
    private Double minOrderValue;

public Restaurant(UUID id, String name, Address address, Double minOrderValue) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.minOrderValue = minOrderValue;
}

public Restaurant(UUID id, String name, String address, Double minOrderValue) {
    this.id = id;
    this.name = name;
    this.address = Address.fromString(address);
    this.minOrderValue = minOrderValue;

}



public UUID getId() {return id;
}
public String getName() {
    return name;
}
public Address getAddress() {
    return address;
}

public Double getMinOrderValue() {
    return minOrderValue;
}

public void setName(String name) {
    this.name = name;
}

public void setAddress(String address) {
    this.address = Address.fromString(address);
}
public void setMinOrderValue(Double minOrderValue) {
    this.minOrderValue = minOrderValue;
}
}


