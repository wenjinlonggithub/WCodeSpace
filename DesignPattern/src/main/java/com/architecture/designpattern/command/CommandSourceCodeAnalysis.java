package com.architecture.designpattern.command;

import org.springframework.stereotype.Component;

@Component
public class CommandSourceCodeAnalysis {
    public void analyzeBasicImplementation() { System.out.println("基础实现：请求封装、解耦调用者和接收者"); }
    public void analyzeThreadPoolExecutor() { System.out.println("线程池：Runnable命令接口，execute方法调用"); }
    public void analyzeSwingAction() { System.out.println("Swing Action：UI事件命令化，菜单和按钮复用"); }
    public void analyzeJdbcTemplate() { System.out.println("JdbcTemplate：callback命令模式，数据库操作封装"); }
    public void analyzeMacroCommand() { System.out.println("宏命令：组合多个命令，批量执行和撤销"); }
}