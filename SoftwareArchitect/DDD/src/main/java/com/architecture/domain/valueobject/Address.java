package com.architecture.domain.valueobject;

import java.util.Objects;

public class Address {
    private final String province;
    private final String city;
    private final String district;
    private final String street;
    private final String zipCode;

    public Address(String province, String city, String district, String street, String zipCode) {
        if (province == null || city == null || street == null) {
            throw new IllegalArgumentException("地址信息不完整");
        }
        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.zipCode = zipCode;
    }

    public String getFullAddress() {
        return province + city + (district != null ? district : "") + street;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(province, address.province) &&
                Objects.equals(city, address.city) &&
                Objects.equals(district, address.district) &&
                Objects.equals(street, address.street) &&
                Objects.equals(zipCode, address.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(province, city, district, street, zipCode);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
