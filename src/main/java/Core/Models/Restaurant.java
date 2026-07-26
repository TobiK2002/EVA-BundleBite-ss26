package Core.Models;


import java.util.UUID;

public class Restaurant {
    private final UUID id;
    private String name;
    private String address;
    private Double minOrderValue;

public Restaurant(UUID id, String name, String address, Double minOrderValue) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.minOrderValue = minOrderValue;
}

public UUID getId() {return id;
}
public String getName() {
    return name;
}
public String getAddress() {
    return address;
}

}


