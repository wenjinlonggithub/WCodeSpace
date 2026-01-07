package com.architecture.designpattern.facade;

import org.springframework.stereotype.Component;

@Component
public class FacadeInterviewQuestions {
    public void whatIsFacade() { System.out.println("外观模式：为子系统中一组接口提供一个一致的界面"); }
    public void coreComponents() { System.out.println("核心组件：外观类、子系统类、客户端"); }
    public void applicableScenarios() { System.out.println("适用场景：简化复杂系统、分层架构、第三方库封装"); }
    public void vsAdapterPattern() { System.out.println("与适配器区别：外观简化接口，适配器转换接口"); }
    public void designPrinciples() { System.out.println("设计原则：最少知识原则、降低耦合度"); }
}