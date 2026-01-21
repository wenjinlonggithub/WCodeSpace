package com.hospital.gateway.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.hospital.gateway.response.SimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/server")
@Slf4j
public class ServerController {

    @Autowired(required = false)
    private NamingService namingService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    /**
     * 心跳检测接口
     * 用于检查服务是否正常运行
     *
     * @return SimpleResponse<String> 成功状态响应
     * @author AI Assistant
     * @date 2024-12-26
     */
    @GetMapping("/health")
    public SimpleResponse<String> health() {
        log.info("心跳检测接口被调用");
        return SimpleResponse.succeed("服务运行正常");
    }

    /**
     * Nacos服务下线接口
     * 访问后将当前服务从Nacos注册中心下线
     *
     * @return SimpleResponse<String> 下线结果响应
     * @author AI Assistant
     * @date 2024-12-26
     */
    @PostMapping("/shutdown")
    public SimpleResponse<String> shutdown() {
        log.warn("Nacos服务下线接口被调用，准备下线服务");

        try {
            if (namingService == null) {
                log.warn("NamingService未注入，可能未启用Nacos服务发现");
                return SimpleResponse.succeed("服务未注册到Nacos或Nacos未启用，无需下线");
            }

            // 获取本机IP地址
            String localHost = InetAddress.getLocalHost().getHostAddress();

            // 从Nacos注册中心下线当前服务实例
            namingService.deregisterInstance(applicationName, localHost, serverPort);

            log.info("服务已从Nacos成功下线，服务名：{}，IP：{}，端口：{}",
                    applicationName, localHost, serverPort);

            return SimpleResponse.succeed("服务已成功从Nacos下线");

        } catch (NacosException e) {
            log.error("从Nacos下线服务失败：{}", e.getMessage(), e);
            return SimpleResponse.fail("从Nacos下线服务失败：" + e.getMessage());
        } catch (UnknownHostException e) {
            log.error("获取本机IP地址失败：{}", e.getMessage(), e);
            return SimpleResponse.fail("获取本机IP地址失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("下线服务时发生未知异常：{}", e.getMessage(), e);
            return SimpleResponse.fail("下线服务时发生异常：" + e.getMessage());
        }
    }
}
