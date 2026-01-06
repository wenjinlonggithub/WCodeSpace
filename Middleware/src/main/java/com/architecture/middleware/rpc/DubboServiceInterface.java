package com.architecture.middleware.rpc;

import com.architecture.middleware.rpc.dto.User;

public interface DubboServiceInterface {
    String sayHello(String name);
    User getUserById(Long id);
    String processData(String data);
}