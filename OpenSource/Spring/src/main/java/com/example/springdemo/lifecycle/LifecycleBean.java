package com.example.springdemo.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * Bean生命周期演示类
 * 
 * 演示Spring Bean完整生命周期的各个阶段
 */
@Slf4j
@Component
public class LifecycleBean implements BeanNameAware, ApplicationContextAware, 
        InitializingBean, DisposableBean {
    
    private String beanName;
    private ApplicationContext applicationContext;
    private boolean initialized = false;
    
    public LifecycleBean() {
        log.info("🏗️  [1] 构造器调用 - LifecycleBean实例化");
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("🧠 [2] BeanNameAware.setBeanName() - Bean名称: {}", name);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("🧠 [3] ApplicationContextAware.setApplicationContext() - 容器注入成功");
    }
    
    @PostConstruct
    public void postConstruct() {
        log.info("🚀 [4] @PostConstruct - 初始化方法1");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.initialized = true;
        log.info("🚀 [5] InitializingBean.afterPropertiesSet() - 初始化方法2");
    }
    
    @PreDestroy
    public void preDestroy() {
        log.info("💀 [6] @PreDestroy - 销毁前处理");
    }
    
    @Override
    public void destroy() throws Exception {
        log.info("💀 [7] DisposableBean.destroy() - 销毁处理");
    }
    
    public void performAction(String action) {
        log.info("💼 执行业务操作: {}", action);
    }
    
    public void showStatus() {
        log.info("📊 LifecycleBean状态:");
        log.info("   Bean名称: {}", beanName);
        log.info("   容器注入: {}", applicationContext != null ? "成功" : "失败");
        log.info("   初始化完成: {}", initialized);
    }
    
    public boolean hasApplicationContext() {
        return applicationContext != null;
    }
    
    public boolean hasBeanName() {
        return beanName != null && !beanName.isEmpty();
    }
    
    public String getBeanName() {
        return beanName;
    }
}