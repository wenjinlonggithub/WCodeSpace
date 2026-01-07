package com.architecture.designpattern.abstractfactory;

public class AbstractFactoryExample {
    
    public interface UIFactory {
        Button createButton();
        TextField createTextField();
    }
    
    public interface Button {
        void click();
    }
    
    public interface TextField {
        void type(String text);
    }
    
    public static class WindowsUIFactory implements UIFactory {
        @Override
        public Button createButton() {
            return new WindowsButton();
        }
        
        @Override
        public TextField createTextField() {
            return new WindowsTextField();
        }
    }
    
    public static class MacUIFactory implements UIFactory {
        @Override
        public Button createButton() {
            return new MacButton();
        }
        
        @Override
        public TextField createTextField() {
            return new MacTextField();
        }
    }
    
    public static class WindowsButton implements Button {
        @Override
        public void click() {
            System.out.println("Windows button clicked");
        }
    }
    
    public static class MacButton implements Button {
        @Override
        public void click() {
            System.out.println("Mac button clicked");
        }
    }
    
    public static class WindowsTextField implements TextField {
        @Override
        public void type(String text) {
            System.out.println("Windows text field: " + text);
        }
    }
    
    public static class MacTextField implements TextField {
        @Override
        public void type(String text) {
            System.out.println("Mac text field: " + text);
        }
    }
    
    public static class Application {
        private Button button;
        private TextField textField;
        
        public Application(UIFactory factory) {
            this.button = factory.createButton();
            this.textField = factory.createTextField();
        }
        
        public void run() {
            button.click();
            textField.type("Hello World");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern Demo ===\n");
        
        System.out.println("Windows UI:");
        UIFactory windowsFactory = new WindowsUIFactory();
        Application windowsApp = new Application(windowsFactory);
        windowsApp.run();
        
        System.out.println("\nMac UI:");
        UIFactory macFactory = new MacUIFactory();
        Application macApp = new Application(macFactory);
        macApp.run();
    }
}