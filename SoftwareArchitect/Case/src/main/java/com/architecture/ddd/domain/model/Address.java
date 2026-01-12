package com.architecture.ddd.domain.model;

import java.util.Objects;

/**
 * DDD - 地址值对象
 */
public class Address {

    private final String province;
    private final String city;
    private final String district;
    private final String street;
    private final String detail;

    private Address(String province, String city, String district, String street, String detail) {
        if (province == null || city == null || detail == null) {
            throw new IllegalArgumentException("地址信息不完整");
        }
        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.detail = detail;
    }

    public static Address of(String province, String city, String district, String street, String detail) {
        return new Address(province, city, district, street, detail);
    }

    public String getFullAddress() {
        return province + city + (district != null ? district : "") +
               (street != null ? street : "") + detail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address address = (Address) o;
        return Objects.equals(province, address.province) &&
                Objects.equals(city, address.city) &&
                Objects.equals(district, address.district) &&
                Objects.equals(street, address.street) &&
                Objects.equals(detail, address.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(province, city, district, street, detail);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
