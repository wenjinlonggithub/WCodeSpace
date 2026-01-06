package com.architecture.middleware.rpc;

public interface DubboServiceInterface {
    String sayHello(String name);
    DubboServiceImpl.User getUserById(Long id);
    String processData(String data);
}