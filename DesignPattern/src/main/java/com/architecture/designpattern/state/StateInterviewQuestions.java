package com.architecture.designpattern.state;

import org.springframework.stereotype.Component;

@Component
public class StateInterviewQuestions {
    public void whatIsState() { System.out.println("状态模式：允许对象在内部状态改变时改变它的行为"); }
    public void coreComponents() { System.out.println("核心组件：上下文、状态接口、具体状态"); }
    public void applicableScenarios() { System.out.println("适用场景：状态机、工作流、游戏状态、TCP连接"); }
    public void vsStrategyPattern() { System.out.println("与策略模式区别：状态自动转换，策略由客户端选择"); }
    public void stateTransition() { System.out.println("状态转换：状态图、转换条件、状态管理"); }
}