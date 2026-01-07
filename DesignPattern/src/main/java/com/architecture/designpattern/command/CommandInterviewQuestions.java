package com.architecture.designpattern.command;

import org.springframework.stereotype.Component;

@Component
public class CommandInterviewQuestions {
    public void whatIsCommand() { System.out.println("命令模式：将请求封装成对象，使你可以用不同请求对客户进行参数化"); }
    public void coreComponents() { System.out.println("核心组件：命令接口、具体命令、调用者、接收者"); }
    public void applicableScenarios() { System.out.println("适用场景：撤销操作、宏命令、队列请求、日志请求"); }
    public void undoImplementation() { System.out.println("撤销实现：命令栈、逆向操作、状态备份"); }
    public void macroCommand() { System.out.println("宏命令：组合模式、批量执行、事务处理"); }
}