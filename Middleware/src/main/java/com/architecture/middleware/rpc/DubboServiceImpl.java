package com.architecture.middleware.rpc;

import com.architecture.middleware.rpc.dto.User;
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
}