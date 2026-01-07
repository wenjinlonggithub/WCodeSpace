package com.architecture.designpattern.flyweight;

import java.util.HashMap;
import java.util.Map;

public class FlyweightExample {
    
    public interface Shape {
        void draw(int x, int y, String color);
    }
    
    public static class Circle implements Shape {
        private String type;
        private int radius;
        
        public Circle(String type, int radius) {
            this.type = type;
            this.radius = radius;
            System.out.println("Creating circle of type: " + type);
        }
        
        @Override
        public void draw(int x, int y, String color) {
            System.out.println("Drawing " + type + " circle at (" + x + ", " + y + 
                             ") with color " + color + " and radius " + radius);
        }
    }
    
    public static class ShapeFactory {
        private static Map<String, Shape> shapes = new HashMap<>();
        
        public static Shape getCircle(String type, int radius) {
            String key = type + "_" + radius;
            Shape shape = shapes.get(key);
            
            if (shape == null) {
                shape = new Circle(type, radius);
                shapes.put(key, shape);
            }
            
            return shape;
        }
        
        public static int getShapeCount() {
            return shapes.size();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Flyweight Pattern Demo ===\n");
        
        String[] types = {"Large", "Medium", "Small"};
        String[] colors = {"Red", "Green", "Blue", "Yellow"};
        int[] radii = {10, 15, 20};
        
        for (int i = 0; i < 10; i++) {
            String type = types[(int) (Math.random() * types.length)];
            int radius = radii[(int) (Math.random() * radii.length)];
            String color = colors[(int) (Math.random() * colors.length)];
            
            Shape circle = ShapeFactory.getCircle(type, radius);
            circle.draw((int) (Math.random() * 100), (int) (Math.random() * 100), color);
        }
        
        System.out.println("\nTotal shape objects created: " + ShapeFactory.getShapeCount());
    }
}