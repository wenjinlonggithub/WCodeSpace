package com.architecture.designpattern.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CommandExample {
    
    public void demonstratePattern() {
        System.out.println("=== 命令模式演示 ===");
        
        // 遥控器演示
        System.out.println("1. 遥控器演示:");
        
        Light livingRoomLight = new Light("客厅");
        Stereo stereo = new Stereo();
        
        Command lightOn = new LightOnCommand(livingRoomLight);
        Command lightOff = new LightOffCommand(livingRoomLight);
        Command stereoOn = new StereoOnCommand(stereo);
        Command stereoOff = new StereoOffCommand(stereo);
        
        RemoteControl remote = new RemoteControl();
        remote.setCommand(0, lightOn, lightOff);
        remote.setCommand(1, stereoOn, stereoOff);
        
        remote.onButtonPressed(0);
        remote.offButtonPressed(0);
        remote.undoButtonPressed();
        
        remote.onButtonPressed(1);
        remote.offButtonPressed(1);
        
        // 宏命令演示
        System.out.println("\n2. 宏命令演示:");
        Command[] partyOn = {lightOn, stereoOn};
        Command[] partyOff = {lightOff, stereoOff};
        
        MacroCommand partyOnMacro = new MacroCommand(partyOn);
        MacroCommand partyOffMacro = new MacroCommand(partyOff);
        
        remote.setCommand(2, partyOnMacro, partyOffMacro);
        remote.onButtonPressed(2);
        remote.offButtonPressed(2);
        
        // 文本编辑器演示
        System.out.println("\n3. 文本编辑器演示:");
        TextEditor editor = new TextEditor();
        TextEditorInvoker invoker = new TextEditorInvoker();
        
        invoker.execute(new WriteCommand(editor, "Hello "));
        invoker.execute(new WriteCommand(editor, "World!"));
        invoker.execute(new DeleteCommand(editor, 6)); // 删除"World!"
        
        invoker.undo();
        invoker.undo();
        invoker.redo();
    }
}

// 命令接口
interface Command {
    void execute();
    void undo();
}

// 接收者类
class Light {
    private String location;
    private boolean isOn = false;
    
    public Light(String location) {
        this.location = location;
    }
    
    public void on() {
        isOn = true;
        System.out.println("💡 " + location + "的灯打开了");
    }
    
    public void off() {
        isOn = false;
        System.out.println("💡 " + location + "的灯关闭了");
    }
}

class Stereo {
    private boolean isOn = false;
    private int volume = 0;
    
    public void on() {
        isOn = true;
        System.out.println("🔊 音响打开了");
    }
    
    public void off() {
        isOn = false;
        System.out.println("🔊 音响关闭了");
    }
    
    public void setVolume(int volume) {
        this.volume = volume;
        System.out.println("🔊 音量设置为: " + volume);
    }
}

// 具体命令类
class LightOnCommand implements Command {
    private Light light;
    
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.on();
    }
    
    @Override
    public void undo() {
        light.off();
    }
}

class LightOffCommand implements Command {
    private Light light;
    
    public LightOffCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.off();
    }
    
    @Override
    public void undo() {
        light.on();
    }
}

class StereoOnCommand implements Command {
    private Stereo stereo;
    
    public StereoOnCommand(Stereo stereo) {
        this.stereo = stereo;
    }
    
    @Override
    public void execute() {
        stereo.on();
        stereo.setVolume(11);
    }
    
    @Override
    public void undo() {
        stereo.off();
    }
}

class StereoOffCommand implements Command {
    private Stereo stereo;
    
    public StereoOffCommand(Stereo stereo) {
        this.stereo = stereo;
    }
    
    @Override
    public void execute() {
        stereo.off();
    }
    
    @Override
    public void undo() {
        stereo.on();
        stereo.setVolume(11);
    }
}

// 空命令类（空对象模式）
class NoCommand implements Command {
    @Override
    public void execute() {}
    
    @Override
    public void undo() {}
}

// 宏命令
class MacroCommand implements Command {
    private Command[] commands;
    
    public MacroCommand(Command[] commands) {
        this.commands = commands;
    }
    
    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }
    
    @Override
    public void undo() {
        for (int i = commands.length - 1; i >= 0; i--) {
            commands[i].undo();
        }
    }
}

// 调用者（遥控器）
class RemoteControl {
    private Command[] onCommands;
    private Command[] offCommands;
    private Command undoCommand;
    
    public RemoteControl() {
        onCommands = new Command[7];
        offCommands = new Command[7];
        
        Command noCommand = new NoCommand();
        for (int i = 0; i < 7; i++) {
            onCommands[i] = noCommand;
            offCommands[i] = noCommand;
        }
        undoCommand = noCommand;
    }
    
    public void setCommand(int slot, Command onCommand, Command offCommand) {
        onCommands[slot] = onCommand;
        offCommands[slot] = offCommand;
    }
    
    public void onButtonPressed(int slot) {
        onCommands[slot].execute();
        undoCommand = onCommands[slot];
    }
    
    public void offButtonPressed(int slot) {
        offCommands[slot].execute();
        undoCommand = offCommands[slot];
    }
    
    public void undoButtonPressed() {
        undoCommand.undo();
    }
}

// 文本编辑器示例
class TextEditor {
    private StringBuilder content = new StringBuilder();
    
    public void write(String text) {
        content.append(text);
        System.out.println("✏️ 写入文本: '" + text + "' 当前内容: '" + content + "'");
    }
    
    public void delete(int length) {
        if (length <= content.length()) {
            String deleted = content.substring(content.length() - length);
            content.delete(content.length() - length, content.length());
            System.out.println("🗑️ 删除文本: '" + deleted + "' 当前内容: '" + content + "'");
        }
    }
    
    public String getContent() {
        return content.toString();
    }
}

class WriteCommand implements Command {
    private TextEditor editor;
    private String text;
    
    public WriteCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text = text;
    }
    
    @Override
    public void execute() {
        editor.write(text);
    }
    
    @Override
    public void undo() {
        editor.delete(text.length());
    }
}

class DeleteCommand implements Command {
    private TextEditor editor;
    private int length;
    private String deletedText;
    
    public DeleteCommand(TextEditor editor, int length) {
        this.editor = editor;
        this.length = length;
    }
    
    @Override
    public void execute() {
        String content = editor.getContent();
        if (length <= content.length()) {
            deletedText = content.substring(content.length() - length);
            editor.delete(length);
        }
    }
    
    @Override
    public void undo() {
        if (deletedText != null) {
            editor.write(deletedText);
        }
    }
}

class TextEditorInvoker {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();
    
    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // 执行新命令后清除重做栈
    }
    
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            System.out.println("⏪ 撤销操作");
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            System.out.println("⏩ 重做操作");
        }
    }
}