package Core.Models;

import Core.Models.exceptions.AddressException;

import java.util.Arrays;
import java.util.Objects;

public class Address {
    private final String street;
    private final String houseNumber;
    private final String postalCode;
    private final String city;

    public Address(String street, String houseNumber, String postalCode, String city) {
        validateStreet(street);
        validateHouseNumber(houseNumber);
        validatePostalCode(postalCode);
        validateCity(city);

        this.street = street.trim();
        this.houseNumber = houseNumber.trim();
        this.postalCode = postalCode.trim();
        this.city = city.trim();
    }

    public static Address fromString(String address) {
        if (address == null) {
            throw AddressException.invalidAddress();
        }

        String[] parts = address.trim().split("\\s+");

        if (parts.length < 4) {
            throw AddressException.invalidAddress();
        }

        int houseNumberIndex = parts.length - 3;

        String street = String.join(" ", Arrays.copyOfRange(parts, 0, houseNumberIndex));
        String houseNumber = parts[houseNumberIndex];
        String postalCode = parts[houseNumberIndex + 1];
        String city = parts[houseNumberIndex + 2];

        return new Address(street, houseNumber, postalCode, city);
    }

    public boolean hasSamePostalCode(Address other) {
        return other != null && postalCode.equals(other.postalCode);
    }

    public boolean hasSameCity(Address other) {
        return other != null && city.equalsIgnoreCase(other.city);
    }

    public boolean isInSameArea(Address other) {
        return hasSamePostalCode(other) || hasSameCity(other);
    }

    public String asSingleLine() {
        return street + " " + houseNumber + " " + postalCode + " " + city;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    private void validateStreet(String street) {
        if (street == null || street.isBlank()) {
            throw AddressException.invalidStreet();
        }
    }

    private void validateHouseNumber(String houseNumber) {
        if (houseNumber == null || !houseNumber.matches("\\d+[A-Za-z]?")) {
            throw AddressException.invalidHouseNumber();
        }
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || !postalCode.matches("\\d{5}")) {
            throw AddressException.invalidPostalCode();
        }
    }

    private void validateCity(String city) {
        if (city == null || !city.matches("[A-Za-zÄÖÜäöüß\\-]+")) {
            throw AddressException.invalidCity();
        }
    }

    @Override
    public String toString() {
        return asSingleLine();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(street, address.street)
                && Objects.equals(houseNumber, address.houseNumber)
                && Objects.equals(postalCode, address.postalCode)
                && Objects.equals(city, address.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, houseNumber, postalCode, city);
    }
}
