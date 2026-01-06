package com.example.springdemo;

import com.example.springdemo.ioc.IoCDemoService;
import com.example.springdemo.di.DIDemoService;
import com.example.springdemo.lifecycle.LifecycleDemoService;
import com.example.springdemo.events.EventsDemoService;
import com.example.springdemo.aop.AOPDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring Demo 演示运行器
 * 
 * 统一管理和运行所有Spring特性演示
 * 实现CommandLineRunner，在应用启动后自动运行演示
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDemoRunner implements CommandLineRunner {
    
    private final IoCDemoService iocDemoService;
    private final DIDemoService diDemoService;
    private final LifecycleDemoService lifecycleDemoService;
    private final EventsDemoService eventsDemoService;
    private final AOPDemoService aopDemoService;
    
    @Override
    public void run(String... args) throws Exception {
        runAllDemos();
    }
    
    public void runAllDemos() {
        try {
            log.info("\n🚀 开始Spring框架特性演示...\n");
            
            // 1. IoC容器演示
            runIoCDemo();
            
            // 2. 依赖注入演示  
            runDIDemo();
            
            // 3. Bean生命周期演示
            runLifecycleDemo();
            
            // 4. 事件机制演示
            runEventsDemo();
            
            // 5. AOP演示
            runAOPDemo();
            
            log.info("\n✅ 所有演示完成！");
            
        } catch (Exception e) {
            log.error("演示运行出错", e);
        }
    }
    
    private void runIoCDemo() {
        log.info("\n" + "=".repeat(50));
        log.info("         📦 IoC容器演示");
        log.info("=".repeat(50));
        iocDemoService.demonstrateIoC();
    }
    
    private void runDIDemo() {
        log.info("\n" + "=".repeat(50));
        log.info("         💉 依赖注入演示"); 
        log.info("=".repeat(50));
        diDemoService.demonstrateDI();
    }
    
    private void runLifecycleDemo() {
        log.info("\n" + "=".repeat(50));
        log.info("         🔄 Bean生命周期演示");
        log.info("=".repeat(50));
        lifecycleDemoService.demonstrateLifecycle();
    }
    
    private void runEventsDemo() {
        log.info("\n" + "=".repeat(50));
        log.info("         📢 事件机制演示");
        log.info("=".repeat(50));
        eventsDemoService.demonstrateEvents();
    }
    
    private void runAOPDemo() {
        log.info("\n" + "=".repeat(50));
        log.info("         🎯 AOP演示");
        log.info("=".repeat(50));
        aopDemoService.demonstrateAOP();
    }
}