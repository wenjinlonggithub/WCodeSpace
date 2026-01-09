package com.architecture.domain.entity;

import com.architecture.domain.valueobject.Email;
import java.time.LocalDateTime;
import java.util.Objects;

public class Customer {
    private Long id;
    private String name;
    private Email email;
    private String phone;
    private CustomerLevel level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer(Long id, String name, Email email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.level = CustomerLevel.NORMAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void upgradeLevel() {
        if (this.level == CustomerLevel.NORMAL) {
            this.level = CustomerLevel.VIP;
        } else if (this.level == CustomerLevel.VIP) {
            this.level = CustomerLevel.SVIP;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContactInfo(Email email, String phone) {
        this.email = email;
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public CustomerLevel getLevel() {
        return level;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
