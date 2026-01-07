package com.architecture.designpattern.mediator;

import java.util.ArrayList;
import java.util.List;

public class MediatorExample {
    
    public interface ChatMediator {
        void sendMessage(String message, User user);
        void addUser(User user);
    }
    
    public abstract static class User {
        protected ChatMediator mediator;
        protected String name;
        
        public User(ChatMediator mediator, String name) {
            this.mediator = mediator;
            this.name = name;
        }
        
        public abstract void send(String message);
        public abstract void receive(String message);
        
        public String getName() {
            return name;
        }
    }
    
    public static class ChatRoom implements ChatMediator {
        private List<User> users;
        
        public ChatRoom() {
            this.users = new ArrayList<>();
        }
        
        @Override
        public void addUser(User user) {
            users.add(user);
            System.out.println(user.getName() + " joined the chat room");
        }
        
        @Override
        public void sendMessage(String message, User sender) {
            System.out.println("[" + sender.getName() + "]: " + message);
            for (User user : users) {
                if (user != sender) {
                    user.receive(message);
                }
            }
        }
    }
    
    public static class ConcreteUser extends User {
        
        public ConcreteUser(ChatMediator mediator, String name) {
            super(mediator, name);
        }
        
        @Override
        public void send(String message) {
            mediator.sendMessage(message, this);
        }
        
        @Override
        public void receive(String message) {
            System.out.println("  " + name + " received: " + message);
        }
    }
    
    public static class AdminUser extends User {
        
        public AdminUser(ChatMediator mediator, String name) {
            super(mediator, name);
        }
        
        @Override
        public void send(String message) {
            System.out.println("ADMIN MESSAGE:");
            mediator.sendMessage(message, this);
        }
        
        @Override
        public void receive(String message) {
            System.out.println("  [ADMIN] " + name + " received: " + message);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Mediator Pattern Demo ===\n");
        
        ChatMediator chatRoom = new ChatRoom();
        
        User alice = new ConcreteUser(chatRoom, "Alice");
        User bob = new ConcreteUser(chatRoom, "Bob");
        User charlie = new ConcreteUser(chatRoom, "Charlie");
        User admin = new AdminUser(chatRoom, "Admin");
        
        chatRoom.addUser(alice);
        chatRoom.addUser(bob);
        chatRoom.addUser(charlie);
        chatRoom.addUser(admin);
        
        System.out.println();
        
        alice.send("Hello everyone!");
        System.out.println();
        
        bob.send("Hi Alice! How are you?");
        System.out.println();
        
        admin.send("Please keep the conversation appropriate.");
        System.out.println();
        
        charlie.send("Welcome to the chat room!");
    }
}