package com.architecture.middleware.rpc;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class DubboConsumerExample {

    @DubboReference
    private DubboServiceInterface dubboService;

    public void testDubboService() {
        String result = dubboService.sayHello("World");
        System.out.println("Dubbo service result: " + result);

        DubboServiceImpl.User user = dubboService.getUserById(1L);
        System.out.println("Dubbo user: " + user.getName());

        String processResult = dubboService.processData("test data");
        System.out.println("Dubbo process result: " + processResult);
    }
}