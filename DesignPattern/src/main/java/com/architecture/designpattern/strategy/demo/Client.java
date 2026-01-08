package com.architecture.designpattern.strategy.demo;

public class Client {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setStrategy(new Addition());
        System.out.println(calculator.getResult(1, 1));

        calculator.setStrategy(new Subtraction());
        System.out.println(calculator.getResult(1, 1));
    }
}
