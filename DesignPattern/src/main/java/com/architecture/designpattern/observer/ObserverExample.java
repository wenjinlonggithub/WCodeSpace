package com.architecture.designpattern.observer;

import java.util.ArrayList;
import java.util.List;

public class ObserverExample {
    
    public void demonstratePattern() {
        System.out.println("=== 观察者模式演示 ===");
        
        // 创建被观察者
        WeatherStation weatherStation = new WeatherStation();
        
        // 创建观察者
        CurrentConditionsDisplay currentDisplay = new CurrentConditionsDisplay();
        StatisticsDisplay statisticsDisplay = new StatisticsDisplay();
        ForecastDisplay forecastDisplay = new ForecastDisplay();
        
        // 注册观察者
        weatherStation.addObserver(currentDisplay);
        weatherStation.addObserver(statisticsDisplay);
        weatherStation.addObserver(forecastDisplay);
        
        // 更新天气数据
        System.out.println("第一次天气更新:");
        weatherStation.setMeasurements(25.0f, 65.0f, 1013.25f);
        
        System.out.println("\n第二次天气更新:");
        weatherStation.setMeasurements(28.0f, 70.0f, 1012.8f);
        
        // 移除一个观察者
        System.out.println("\n移除统计显示器后:");
        weatherStation.removeObserver(statisticsDisplay);
        weatherStation.setMeasurements(22.0f, 60.0f, 1014.1f);
    }
}

// 观察者接口
interface Observer {
    void update(float temperature, float humidity, float pressure);
}

// 被观察者接口
interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

// 具体被观察者 - 天气站
class WeatherStation implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private float temperature;
    private float humidity;
    private float pressure;
    
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }
    
    public void measurementsChanged() {
        notifyObservers();
    }
    
    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();
    }
}

// 具体观察者 - 当前状况显示器
class CurrentConditionsDisplay implements Observer {
    private float temperature;
    private float humidity;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        display();
    }
    
    public void display() {
        System.out.println("当前状况: " + temperature + "°C, 湿度: " + humidity + "%");
    }
}

// 具体观察者 - 统计显示器
class StatisticsDisplay implements Observer {
    private List<Float> temperatureHistory = new ArrayList<>();
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        temperatureHistory.add(temperature);
        display();
    }
    
    public void display() {
        if (!temperatureHistory.isEmpty()) {
            float avg = (float) temperatureHistory.stream()
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(0.0);
            float max = temperatureHistory.stream()
                .max(Float::compareTo)
                .orElse(0.0f);
            float min = temperatureHistory.stream()
                .min(Float::compareTo)
                .orElse(0.0f);
            
            System.out.println("统计数据 - 平均: " + String.format("%.1f", avg) + 
                             "°C, 最高: " + max + "°C, 最低: " + min + "°C");
        }
    }
}

// 具体观察者 - 预报显示器
class ForecastDisplay implements Observer {
    private float currentPressure = 29.92f;
    private float lastPressure;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        lastPressure = currentPressure;
        currentPressure = pressure;
        display();
    }
    
    public void display() {
        String forecast;
        if (currentPressure > lastPressure) {
            forecast = "天气正在好转!";
        } else if (currentPressure == lastPressure) {
            forecast = "天气稳定";
        } else {
            forecast = "小心，可能有暴风雨";
        }
        System.out.println("天气预报: " + forecast);
    }
}