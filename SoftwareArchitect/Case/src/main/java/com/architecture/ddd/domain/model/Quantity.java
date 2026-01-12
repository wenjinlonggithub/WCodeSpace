package com.architecture.ddd.domain.model;

import java.util.Objects;

/**
 * DDD - 数量值对象
 */
public class Quantity {

    private final int value;

    private Quantity(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity)) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
