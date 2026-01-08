package com.architecture.designpattern.strategy.demo;

public class Subtraction implements Strategy {
    @Override
    public int calculate(int a, int b) {
        return a - b;
    }
}
