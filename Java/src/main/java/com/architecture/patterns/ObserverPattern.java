package com.architecture.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者模式实现
 * 
 * 核心原理：
 * 1. 定义对象间一对多的依赖关系
 * 2. 当一个对象状态改变时，所有依赖它的对象都会得到通知
 * 3. 实现松耦合设计
 */

// 观察者接口
interface Observer {
    void update(String message);
    String getName();
}

// 被观察者接口
interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String message);
}

// 具体观察者
class ConcreteObserver implements Observer {
    private String name;
    
    public ConcreteObserver(String name) {
        this.name = name;
    }
    
    @Override
    public void update(String message) {
        System.out.println(name + " received message: " + message);
    }
    
    @Override
    public String getName() {
        return name;
    }
}

// 具体被观察者
class ConcreteSubject implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private String state;
    
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer " + observer.getName() + " attached");
        }
    }
    
    @Override
    public void detach(Observer observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer " + observer.getName() + " detached");
        }
    }
    
    @Override
    public void notifyObservers(String message) {
        System.out.println("Notifying " + observers.size() + " observers...");
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    public void setState(String state) {
        this.state = state;
        notifyObservers("State changed to: " + state);
    }
    
    public String getState() {
        return state;
    }
}

// 新闻发布系统示例
class NewsAgency implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private String news;
    
    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    public void setNews(String news) {
        this.news = news;
        notifyObservers("Breaking News: " + news);
    }
}

class NewsChannel implements Observer {
    private String channelName;
    
    public NewsChannel(String channelName) {
        this.channelName = channelName;
    }
    
    @Override
    public void update(String message) {
        System.out.println(channelName + " broadcasting: " + message);
    }
    
    @Override
    public String getName() {
        return channelName;
    }
}

public class ObserverPattern {
    public static void main(String[] args) {
        // 基本观察者模式演示
        ConcreteSubject subject = new ConcreteSubject();
        
        Observer observer1 = new ConcreteObserver("Observer1");
        Observer observer2 = new ConcreteObserver("Observer2");
        Observer observer3 = new ConcreteObserver("Observer3");
        
        subject.attach(observer1);
        subject.attach(observer2);
        subject.attach(observer3);
        
        subject.setState("Active");
        
        System.out.println("\n--- Detaching Observer2 ---");
        subject.detach(observer2);
        
        subject.setState("Inactive");
        
        System.out.println("\n--- News Agency Example ---");
        // 新闻发布系统演示
        NewsAgency agency = new NewsAgency();
        
        NewsChannel cnn = new NewsChannel("CNN");
        NewsChannel bbc = new NewsChannel("BBC");
        NewsChannel fox = new NewsChannel("FOX");
        
        agency.attach(cnn);
        agency.attach(bbc);
        agency.attach(fox);
        
        agency.setNews("Java 21 Released with New Features!");
        
        System.out.println("\n--- Removing FOX ---");
        agency.detach(fox);
        
        agency.setNews("Spring Boot 3.2 Now Available!");
    }
}