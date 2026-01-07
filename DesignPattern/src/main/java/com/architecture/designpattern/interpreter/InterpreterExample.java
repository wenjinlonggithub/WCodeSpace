package com.architecture.designpattern.interpreter;

import java.util.HashMap;
import java.util.Map;

public class InterpreterExample {
    
    public interface Expression {
        int interpret(Context context);
    }
    
    public static class Context {
        private Map<String, Integer> variables = new HashMap<>();
        
        public void setVariable(String name, int value) {
            variables.put(name, value);
        }
        
        public int getVariable(String name) {
            return variables.getOrDefault(name, 0);
        }
    }
    
    public static class Number implements Expression {
        private int value;
        
        public Number(int value) {
            this.value = value;
        }
        
        @Override
        public int interpret(Context context) {
            return value;
        }
    }
    
    public static class Variable implements Expression {
        private String name;
        
        public Variable(String name) {
            this.name = name;
        }
        
        @Override
        public int interpret(Context context) {
            return context.getVariable(name);
        }
    }
    
    public static class Add implements Expression {
        private Expression left, right;
        
        public Add(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret(Context context) {
            return left.interpret(context) + right.interpret(context);
        }
    }
    
    public static class Subtract implements Expression {
        private Expression left, right;
        
        public Subtract(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret(Context context) {
            return left.interpret(context) - right.interpret(context);
        }
    }
    
    public static class Multiply implements Expression {
        private Expression left, right;
        
        public Multiply(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret(Context context) {
            return left.interpret(context) * right.interpret(context);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Interpreter Pattern Demo ===\n");
        
        Context context = new Context();
        context.setVariable("x", 10);
        context.setVariable("y", 5);
        
        System.out.println("Variables: x = 10, y = 5\n");
        
        Expression expression1 = new Add(new Variable("x"), new Variable("y"));
        System.out.println("x + y = " + expression1.interpret(context));
        
        Expression expression2 = new Subtract(new Variable("x"), new Variable("y"));
        System.out.println("x - y = " + expression2.interpret(context));
        
        Expression expression3 = new Multiply(new Variable("x"), new Number(2));
        System.out.println("x * 2 = " + expression3.interpret(context));
        
        Expression expression4 = new Add(
            new Multiply(new Variable("x"), new Number(2)),
            new Variable("y")
        );
        System.out.println("(x * 2) + y = " + expression4.interpret(context));
    }
}