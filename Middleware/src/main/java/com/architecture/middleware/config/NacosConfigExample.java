package com.architecture.middleware.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.listener.Listener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
public class NacosConfigExample {

    private ConfigService configService;
    private final String dataId = "middleware-config";
    private final String group = "DEFAULT_GROUP";

    public void initNacosConfig() {
        try {
            configService = ConfigFactory.createConfigService("localhost:8848");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConfig() {
        try {
            String content = configService.getConfig(dataId, group, 5000);
            System.out.println("Nacos config content: " + content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void publishConfig(String content) {
        try {
            boolean result = configService.publishConfig(dataId, group, content);
            System.out.println("Nacos publish config result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener() {
        try {
            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("Nacos config changed: " + configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}