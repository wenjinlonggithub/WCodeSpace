package com.architecture.designpattern.state;

import org.springframework.stereotype.Component;

@Component
public class StateSourceCodeAnalysis {
    public void analyzeTCPConnection() { System.out.println("TCP连接：CLOSED、LISTEN、ESTABLISHED等状态"); }
    public void analyzeThreadState() { System.out.println("线程状态：NEW、RUNNABLE、BLOCKED、TERMINATED等"); }
    public void analyzeSpringStateMachine() { System.out.println("Spring状态机：状态转换、事件驱动、状态持久化"); }
    public void analyzeOrderStatus() { System.out.println("订单状态：创建、支付、发货、完成等状态流转"); }
    public void analyzeGameState() { System.out.println("游戏状态：开始、运行、暂停、结束状态管理"); }
}