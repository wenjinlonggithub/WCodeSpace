package com.architecture.designpattern.chainofresponsibility;

public class ChainOfResponsibilityExample {

    public void demonstratePattern() {
        System.out.println("=== 责任链模式演示 ===");
        
        // 创建处理器链
        AbstractHandler infoHandler = new InfoHandler();
        AbstractHandler warnHandler = new WarnHandler();
        AbstractHandler errorHandler = new ErrorHandler();
        
        // 构建责任链
        infoHandler.setNextHandler(warnHandler);
        warnHandler.setNextHandler(errorHandler);
        
        // 测试不同级别的请求
        System.out.println("1. 处理INFO级别日志:");
        infoHandler.handle(new LogRequest("INFO", "系统启动成功"));
        
        System.out.println("\n2. 处理WARN级别日志:");
        infoHandler.handle(new LogRequest("WARN", "内存使用率过高"));
        
        System.out.println("\n3. 处理ERROR级别日志:");
        infoHandler.handle(new LogRequest("ERROR", "数据库连接失败"));
        
        System.out.println("\n=== 请假审批流程演示 ===");
        
        // 创建审批链
        ApprovalHandler teamLeader = new TeamLeaderHandler();
        ApprovalHandler departmentManager = new DepartmentManagerHandler();
        ApprovalHandler generalManager = new GeneralManagerHandler();
        
        // 构建审批链
        teamLeader.setNextHandler(departmentManager);
        departmentManager.setNextHandler(generalManager);
        
        // 测试不同天数的请假申请
        System.out.println("1. 申请1天假:");
        teamLeader.handle(new LeaveRequest("张三", 1));
        
        System.out.println("\n2. 申请5天假:");
        teamLeader.handle(new LeaveRequest("李四", 5));
        
        System.out.println("\n3. 申请15天假:");
        teamLeader.handle(new LeaveRequest("王五", 15));
    }
}

// 抽象处理器
abstract class AbstractHandler {
    protected AbstractHandler nextHandler;
    
    public void setNextHandler(AbstractHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    
    public abstract void handle(Request request);
}

// 请求接口
interface Request {
    String getType();
    String getContent();
}

// 日志请求
class LogRequest implements Request {
    private final String level;
    private final String message;
    
    public LogRequest(String level, String message) {
        this.level = level;
        this.message = message;
    }
    
    @Override
    public String getType() {
        return level;
    }
    
    @Override
    public String getContent() {
        return message;
    }
    
    public String getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }
}

// 具体处理器 - INFO处理器
class InfoHandler extends AbstractHandler {
    @Override
    public void handle(Request request) {
        if (request instanceof LogRequest) {
            LogRequest logRequest = (LogRequest) request;
            if ("INFO".equals(logRequest.getLevel())) {
                System.out.println("InfoHandler处理: " + logRequest.getMessage());
                return;
            }
        }
        
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}

// 具体处理器 - WARN处理器
class WarnHandler extends AbstractHandler {
    @Override
    public void handle(Request request) {
        if (request instanceof LogRequest) {
            LogRequest logRequest = (LogRequest) request;
            if ("WARN".equals(logRequest.getLevel())) {
                System.out.println("WarnHandler处理: " + logRequest.getMessage());
                return;
            }
        }
        
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}

// 具体处理器 - ERROR处理器
class ErrorHandler extends AbstractHandler {
    @Override
    public void handle(Request request) {
        if (request instanceof LogRequest) {
            LogRequest logRequest = (LogRequest) request;
            if ("ERROR".equals(logRequest.getLevel())) {
                System.out.println("ErrorHandler处理: " + logRequest.getMessage());
                return;
            }
        }
        
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}

// 请假申请
class LeaveRequest implements Request {
    private final String name;
    private final int days;
    
    public LeaveRequest(String name, int days) {
        this.name = name;
        this.days = days;
    }
    
    @Override
    public String getType() {
        return "LEAVE";
    }
    
    @Override
    public String getContent() {
        return name + "申请" + days + "天假";
    }
    
    public String getName() {
        return name;
    }
    
    public int getDays() {
        return days;
    }
}

// 审批处理器
abstract class ApprovalHandler {
    protected ApprovalHandler nextHandler;
    
    public void setNextHandler(ApprovalHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    
    public abstract void handle(LeaveRequest request);
}

// 团队领导处理器
class TeamLeaderHandler extends ApprovalHandler {
    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 3) {
            System.out.println("团队领导批准: " + request.getContent());
        } else if (nextHandler != null) {
            nextHandler.handle(request);
        } else {
            System.out.println("无法处理请假申请: " + request.getContent());
        }
    }
}

// 部门经理处理器
class DepartmentManagerHandler extends ApprovalHandler {
    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 7) {
            System.out.println("部门经理批准: " + request.getContent());
        } else if (nextHandler != null) {
            nextHandler.handle(request);
        } else {
            System.out.println("无法处理请假申请: " + request.getContent());
        }
    }
}

// 总经理处理器
class GeneralManagerHandler extends ApprovalHandler {
    @Override
    public void handle(LeaveRequest request) {
        if (request.getDays() <= 30) {
            System.out.println("总经理批准: " + request.getContent());
        } else {
            System.out.println("总经理拒绝: " + request.getContent() + " - 请假天数过长");
        }
    }
}