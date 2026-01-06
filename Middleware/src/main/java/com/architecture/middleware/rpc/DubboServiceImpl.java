package com.architecture.middleware.rpc;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DubboServiceImpl implements DubboServiceInterface {

    @Override
    public String sayHello(String name) {
        return "Hello " + name + " from Dubbo!";
    }

    @Override
    public User getUserById(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User" + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    @Override
    public String processData(String data) {
        return "Processed: " + data.toUpperCase();
    }

    public static class User {
        private Long id;
        private String name;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}