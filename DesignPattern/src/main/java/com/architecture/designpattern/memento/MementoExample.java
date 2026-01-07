package com.architecture.designpattern.memento;

import java.util.ArrayList;
import java.util.List;

public class MementoExample {
    
    public static class Memento {
        private String state;
        private String timestamp;
        
        public Memento(String state) {
            this.state = state;
            this.timestamp = java.time.LocalDateTime.now().toString();
        }
        
        public String getState() {
            return state;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
    }
    
    public static class TextEditor {
        private StringBuilder content;
        
        public TextEditor() {
            this.content = new StringBuilder();
        }
        
        public void write(String text) {
            content.append(text);
            System.out.println("Written: " + text);
            System.out.println("Current content: " + content.toString());
        }
        
        public void deleteLast(int count) {
            if (count > 0 && count <= content.length()) {
                content.delete(content.length() - count, content.length());
                System.out.println("Deleted last " + count + " characters");
                System.out.println("Current content: " + content.toString());
            }
        }
        
        public Memento save() {
            System.out.println("Saving state: " + content.toString());
            return new Memento(content.toString());
        }
        
        public void restore(Memento memento) {
            content = new StringBuilder(memento.getState());
            System.out.println("Restored to state: " + content.toString() + 
                             " (saved at: " + memento.getTimestamp() + ")");
        }
        
        public String getContent() {
            return content.toString();
        }
    }
    
    public static class History {
        private List<Memento> mementos;
        
        public History() {
            this.mementos = new ArrayList<>();
        }
        
        public void push(Memento memento) {
            mementos.add(memento);
        }
        
        public Memento pop() {
            if (!mementos.isEmpty()) {
                return mementos.remove(mementos.size() - 1);
            }
            return null;
        }
        
        public Memento peek() {
            if (!mementos.isEmpty()) {
                return mementos.get(mementos.size() - 1);
            }
            return null;
        }
        
        public int size() {
            return mementos.size();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Memento Pattern Demo ===\n");
        
        TextEditor editor = new TextEditor();
        History history = new History();
        
        editor.write("Hello ");
        history.push(editor.save());
        System.out.println();
        
        editor.write("World!");
        history.push(editor.save());
        System.out.println();
        
        editor.write(" How are you?");
        history.push(editor.save());
        System.out.println();
        
        editor.deleteLast(13);
        System.out.println();
        
        System.out.println("Undoing changes...");
        Memento lastState = history.pop();
        if (lastState != null) {
            editor.restore(lastState);
        }
        System.out.println();
        
        System.out.println("Undoing more changes...");
        Memento prevState = history.pop();
        if (prevState != null) {
            editor.restore(prevState);
        }
        System.out.println();
        
        System.out.println("Final content: " + editor.getContent());
        System.out.println("History size: " + history.size());
    }
}