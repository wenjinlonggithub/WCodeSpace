package com.hospital.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * @author lvzeqiang
 * @date 2022/7/1 15:17
 * @description 动态加载yaml 格式路由
 **/
@Slf4j
@Configuration
public class DynamicRouteConfig {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;
    @Value("${gateway.routes.data-id:routes}")
    private String ROUTE_CONFIG_DATA_ID;


    @PostConstruct
    public void init() {
        try {

            ConfigService configService =
                    NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
            String content = configService.getConfig(ROUTE_CONFIG_DATA_ID, nacosConfigProperties.getGroup(), 10000);
            log.info("第一次加载路由开始，info：{}", content);
            loadRoute(content);
            log.info("第一次加载路由结束");

            configService.addListener(ROUTE_CONFIG_DATA_ID, nacosConfigProperties.getGroup(), new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("更新路由开始，info：{}", configInfo);
                    loadRoute(configInfo);
                    log.info("更新路由完成。");
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

        } catch (Exception ex) {
            log.error("动态加载路由失败", ex);
        }
    }

    private void loadRoute(String routeStr) {
        Yaml yaml = new Yaml();
        var obj = yaml.loadAs(routeStr, GatewayProperties.class);
        obj.getRoutes().forEach(route -> {
            log.info("加载路由：{}", route);
            routeDefinitionWriter.save(Mono.just(route)).subscribe();
        });
    }

}
