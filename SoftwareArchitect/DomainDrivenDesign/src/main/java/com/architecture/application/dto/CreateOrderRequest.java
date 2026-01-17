package com.architecture.application.dto;

import java.math.BigDecimal;

public class CreateOrderRequest {
    private Long customerId;
    private String province;
    private String city;
    private String district;
    private String street;
    private String zipCode;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long customerId, String province, String city, String district, String street, String zipCode) {
        this.customerId = customerId;
        this.province = province;
        this.city = city;
        this.district = district;
        this.street = street;
        this.zipCode = zipCode;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
