package com.architecture.designpattern.state;

public class StateExample {
    
    public void demonstratePattern() {
        System.out.println("=== 状态模式演示 ===");
        
        // 自动售货机演示
        System.out.println("1. 自动售货机演示:");
        
        VendingMachine machine = new VendingMachine(2);
        
        machine.insertQuarter();
        machine.turnCrank();
        
        machine.insertQuarter();
        machine.turnCrank();
        
        machine.insertQuarter();
        machine.turnCrank(); // 已售罄
        
        // 媒体播放器演示
        System.out.println("\n2. 媒体播放器演示:");
        
        MediaPlayer player = new MediaPlayer();
        
        player.play();    // 从停止状态开始播放
        player.pause();   // 暂停
        player.play();    // 从暂停状态恢复播放
        player.stop();    // 停止
        player.pause();   // 尝试在停止状态暂停
        
        // 电梯演示
        System.out.println("\n3. 电梯演示:");
        
        Elevator elevator = new Elevator();
        
        elevator.openDoor();     // 打开门
        elevator.closeDoor();    // 关闭门
        elevator.goUp();         // 上升
        elevator.goDown();       // 下降
        elevator.openDoor();     // 在移动中尝试开门
    }
}

// 1. 自动售货机示例
interface State {
    void insertQuarter(VendingMachine machine);
    void ejectQuarter(VendingMachine machine);
    void turnCrank(VendingMachine machine);
    void dispense(VendingMachine machine);
    String getStateName();
}

class VendingMachine {
    private State soldOutState;
    private State noQuarterState;
    private State hasQuarterState;
    private State soldState;
    
    private State currentState;
    private int count = 0;
    
    public VendingMachine(int count) {
        soldOutState = new SoldOutState();
        noQuarterState = new NoQuarterState();
        hasQuarterState = new HasQuarterState();
        soldState = new SoldState();
        
        this.count = count;
        if (count > 0) {
            currentState = noQuarterState;
        } else {
            currentState = soldOutState;
        }
    }
    
    public void insertQuarter() {
        System.out.println("🪙 投入硬币");
        currentState.insertQuarter(this);
    }
    
    public void ejectQuarter() {
        System.out.println("↩️ 退回硬币");
        currentState.ejectQuarter(this);
    }
    
    public void turnCrank() {
        System.out.println("🔄 转动手柄");
        currentState.turnCrank(this);
        currentState.dispense(this);
    }
    
    public void setState(State state) {
        this.currentState = state;
        System.out.println("🔄 状态变更为: " + state.getStateName());
    }
    
    public void releaseBall() {
        System.out.println("🥤 商品已出货!");
        if (count != 0) {
            count--;
        }
    }
    
    public State getSoldOutState() { return soldOutState; }
    public State getNoQuarterState() { return noQuarterState; }
    public State getHasQuarterState() { return hasQuarterState; }
    public State getSoldState() { return soldState; }
    public int getCount() { return count; }
}

class NoQuarterState implements State {
    @Override
    public void insertQuarter(VendingMachine machine) {
        machine.setState(machine.getHasQuarterState());
    }
    
    @Override
    public void ejectQuarter(VendingMachine machine) {
        System.out.println("❌ 您没有投入硬币");
    }
    
    @Override
    public void turnCrank(VendingMachine machine) {
        System.out.println("❌ 请先投入硬币");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("❌ 请先投入硬币");
    }
    
    @Override
    public String getStateName() {
        return "等待投币";
    }
}

class HasQuarterState implements State {
    @Override
    public void insertQuarter(VendingMachine machine) {
        System.out.println("❌ 您已经投入硬币了");
    }
    
    @Override
    public void ejectQuarter(VendingMachine machine) {
        System.out.println("✅ 硬币已退回");
        machine.setState(machine.getNoQuarterState());
    }
    
    @Override
    public void turnCrank(VendingMachine machine) {
        System.out.println("✅ 转动成功...");
        machine.setState(machine.getSoldState());
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("❌ 请先转动手柄");
    }
    
    @Override
    public String getStateName() {
        return "已投币";
    }
}

class SoldState implements State {
    @Override
    public void insertQuarter(VendingMachine machine) {
        System.out.println("❌ 请等待商品出货");
    }
    
    @Override
    public void ejectQuarter(VendingMachine machine) {
        System.out.println("❌ 商品已售出，无法退币");
    }
    
    @Override
    public void turnCrank(VendingMachine machine) {
        System.out.println("❌ 请不要重复转动手柄");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        machine.releaseBall();
        if (machine.getCount() > 0) {
            machine.setState(machine.getNoQuarterState());
        } else {
            System.out.println("📪 售货机已售罄");
            machine.setState(machine.getSoldOutState());
        }
    }
    
    @Override
    public String getStateName() {
        return "商品出货中";
    }
}

class SoldOutState implements State {
    @Override
    public void insertQuarter(VendingMachine machine) {
        System.out.println("❌ 售货机已售罄，无法投币");
    }
    
    @Override
    public void ejectQuarter(VendingMachine machine) {
        System.out.println("❌ 您没有投入硬币");
    }
    
    @Override
    public void turnCrank(VendingMachine machine) {
        System.out.println("❌ 售货机已售罄");
    }
    
    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("❌ 售货机已售罄");
    }
    
    @Override
    public String getStateName() {
        return "售罄";
    }
}

// 2. 媒体播放器示例
interface PlayerState {
    void play(MediaPlayer player);
    void pause(MediaPlayer player);
    void stop(MediaPlayer player);
    String getStateName();
}

class MediaPlayer {
    private PlayerState stoppedState;
    private PlayerState playingState;
    private PlayerState pausedState;
    
    private PlayerState currentState;
    
    public MediaPlayer() {
        stoppedState = new StoppedState();
        playingState = new PlayingState();
        pausedState = new PausedState();
        
        currentState = stoppedState;
    }
    
    public void play() {
        System.out.println("▶️ 播放");
        currentState.play(this);
    }
    
    public void pause() {
        System.out.println("⏸️ 暂停");
        currentState.pause(this);
    }
    
    public void stop() {
        System.out.println("⏹️ 停止");
        currentState.stop(this);
    }
    
    public void setState(PlayerState state) {
        this.currentState = state;
        System.out.println("🔄 播放器状态: " + state.getStateName());
    }
    
    public PlayerState getStoppedState() { return stoppedState; }
    public PlayerState getPlayingState() { return playingState; }
    public PlayerState getPausedState() { return pausedState; }
}

class StoppedState implements PlayerState {
    @Override
    public void play(MediaPlayer player) {
        System.out.println("✅ 开始播放音乐");
        player.setState(player.getPlayingState());
    }
    
    @Override
    public void pause(MediaPlayer player) {
        System.out.println("❌ 音乐已停止，无法暂停");
    }
    
    @Override
    public void stop(MediaPlayer player) {
        System.out.println("❌ 音乐已经是停止状态");
    }
    
    @Override
    public String getStateName() {
        return "停止";
    }
}

class PlayingState implements PlayerState {
    @Override
    public void play(MediaPlayer player) {
        System.out.println("❌ 音乐已在播放中");
    }
    
    @Override
    public void pause(MediaPlayer player) {
        System.out.println("✅ 音乐已暂停");
        player.setState(player.getPausedState());
    }
    
    @Override
    public void stop(MediaPlayer player) {
        System.out.println("✅ 音乐已停止");
        player.setState(player.getStoppedState());
    }
    
    @Override
    public String getStateName() {
        return "播放中";
    }
}

class PausedState implements PlayerState {
    @Override
    public void play(MediaPlayer player) {
        System.out.println("✅ 继续播放音乐");
        player.setState(player.getPlayingState());
    }
    
    @Override
    public void pause(MediaPlayer player) {
        System.out.println("❌ 音乐已暂停");
    }
    
    @Override
    public void stop(MediaPlayer player) {
        System.out.println("✅ 音乐已停止");
        player.setState(player.getStoppedState());
    }
    
    @Override
    public String getStateName() {
        return "暂停";
    }
}

// 3. 电梯示例
interface ElevatorState {
    void openDoor(Elevator elevator);
    void closeDoor(Elevator elevator);
    void goUp(Elevator elevator);
    void goDown(Elevator elevator);
    String getStateName();
}

class Elevator {
    private ElevatorState idleState;
    private ElevatorState movingState;
    private ElevatorState doorOpenState;
    
    private ElevatorState currentState;
    
    public Elevator() {
        idleState = new IdleState();
        movingState = new MovingState();
        doorOpenState = new DoorOpenState();
        
        currentState = idleState;
        System.out.println("🛗 电梯初始状态: " + currentState.getStateName());
    }
    
    public void openDoor() {
        System.out.println("🚪 开门请求");
        currentState.openDoor(this);
    }
    
    public void closeDoor() {
        System.out.println("🚪 关门请求");
        currentState.closeDoor(this);
    }
    
    public void goUp() {
        System.out.println("⬆️ 上升请求");
        currentState.goUp(this);
    }
    
    public void goDown() {
        System.out.println("⬇️ 下降请求");
        currentState.goDown(this);
    }
    
    public void setState(ElevatorState state) {
        this.currentState = state;
        System.out.println("🔄 电梯状态: " + state.getStateName());
    }
    
    public ElevatorState getIdleState() { return idleState; }
    public ElevatorState getMovingState() { return movingState; }
    public ElevatorState getDoorOpenState() { return doorOpenState; }
}

class IdleState implements ElevatorState {
    @Override
    public void openDoor(Elevator elevator) {
        System.out.println("✅ 门已打开");
        elevator.setState(elevator.getDoorOpenState());
    }
    
    @Override
    public void closeDoor(Elevator elevator) {
        System.out.println("❌ 门已经关闭");
    }
    
    @Override
    public void goUp(Elevator elevator) {
        System.out.println("✅ 电梯上升中");
        elevator.setState(elevator.getMovingState());
    }
    
    @Override
    public void goDown(Elevator elevator) {
        System.out.println("✅ 电梯下降中");
        elevator.setState(elevator.getMovingState());
    }
    
    @Override
    public String getStateName() {
        return "空闲";
    }
}

class MovingState implements ElevatorState {
    @Override
    public void openDoor(Elevator elevator) {
        System.out.println("❌ 电梯移动中，无法开门");
    }
    
    @Override
    public void closeDoor(Elevator elevator) {
        System.out.println("❌ 门已经关闭");
    }
    
    @Override
    public void goUp(Elevator elevator) {
        System.out.println("❌ 电梯已在移动中");
    }
    
    @Override
    public void goDown(Elevator elevator) {
        System.out.println("❌ 电梯已在移动中");
    }
    
    @Override
    public String getStateName() {
        return "移动中";
    }
}

class DoorOpenState implements ElevatorState {
    @Override
    public void openDoor(Elevator elevator) {
        System.out.println("❌ 门已经打开");
    }
    
    @Override
    public void closeDoor(Elevator elevator) {
        System.out.println("✅ 门已关闭");
        elevator.setState(elevator.getIdleState());
    }
    
    @Override
    public void goUp(Elevator elevator) {
        System.out.println("❌ 请先关门");
    }
    
    @Override
    public void goDown(Elevator elevator) {
        System.out.println("❌ 请先关门");
    }
    
    @Override
    public String getStateName() {
        return "门已打开";
    }
}