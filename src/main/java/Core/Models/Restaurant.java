package Core.Models;


import java.util.UUID;

public class Restaurant {
    private final UUID id;
    private String name;
    private String address;

Restaurant(UUID id, String name, String address) {
    this.id = id;
    this.name = name;
    this.address = address;
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


