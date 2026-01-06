package com.architecture.middleware.controller;

import com.architecture.middleware.cache.MemcachedExample;
import com.architecture.middleware.config.ApolloConfigExample;
import com.architecture.middleware.config.NacosConfigExample;
import com.architecture.middleware.database.service.UserService;
import com.architecture.middleware.kafka.KafkaProducer;
import com.architecture.middleware.logging.ElasticsearchExample;
import com.architecture.middleware.mq.ActiveMQExample;
import com.architecture.middleware.mq.RocketMQProducer;
import com.architecture.middleware.mq.ZeroMQExample;
import com.architecture.middleware.rabbitmq.RabbitMQProducer;
import com.architecture.middleware.redis.RedisExample;
import com.architecture.middleware.rpc.DubboConsumerExample;
import com.architecture.middleware.scheduler.QuartzExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/middleware")
public class MiddlewareController {

    @Autowired
    private RedisExample redisExample;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private RocketMQProducer rocketMQProducer;

    @Autowired
    private ActiveMQExample activeMQExample;

    @Autowired
    private ZeroMQExample zeroMQExample;

    @Autowired
    private MemcachedExample memcachedExample;

    @Autowired
    private QuartzExample quartzExample;

    @Autowired
    private DubboConsumerExample dubboConsumerExample;

    @Autowired
    private NacosConfigExample nacosConfigExample;

    @Autowired
    private ApolloConfigExample apolloConfigExample;

    @Autowired
    private ElasticsearchExample elasticsearchExample;

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Middleware demo is running!");
        return result;
    }

    @PostMapping("/redis/set")
    public Map<String, String> redisSet(@RequestParam String key, @RequestParam String value) {
        redisExample.stringOperations();
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Redis operations completed");
        return result;
    }

    @PostMapping("/mq/rabbitmq/send")
    public Map<String, String> sendRabbitMessage(@RequestParam String message) {
        rabbitMQProducer.sendDirectMessage(message);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "RabbitMQ message sent");
        return result;
    }

    @PostMapping("/mq/kafka/send")
    public Map<String, String> sendKafkaMessage(@RequestParam String topic, @RequestParam String message) {
        kafkaProducer.sendMessage(topic, message);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Kafka message sent");
        return result;
    }

    @PostMapping("/mq/rocketmq/send")
    public Map<String, String> sendRocketMessage(@RequestParam String topic, @RequestParam String message) {
        rocketMQProducer.sendMessage(topic, message);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "RocketMQ message sent");
        return result;
    }

    @PostMapping("/mq/activemq/send")
    public Map<String, String> sendActiveMessage(@RequestParam String destination, @RequestParam String message) {
        activeMQExample.sendMessage(destination, message);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "ActiveMQ message sent");
        return result;
    }

    @PostMapping("/cache/memcached/set")
    public Map<String, String> memcachedSet(@RequestParam String key, @RequestParam String value) {
        try {
            memcachedExample.setCache(key, value, 3600);
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Memcached cache set");
            return result;
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    @PostMapping("/scheduler/quartz/schedule")
    public Map<String, String> scheduleJob(@RequestParam String jobName, @RequestParam String cron) {
        try {
            quartzExample.scheduleJob(jobName, "default", cron);
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Job scheduled");
            return result;
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    @GetMapping("/rpc/dubbo/test")
    public Map<String, String> testDubbo() {
        try {
            dubboConsumerExample.testDubboService();
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Dubbo service tested");
            return result;
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    @PostMapping("/logging/elasticsearch/index")
    public Map<String, String> indexLog(@RequestParam String level, @RequestParam String message) {
        elasticsearchExample.indexLog(level, message);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Log indexed to Elasticsearch");
        return result;
    }

    @GetMapping("/config/apollo/get")
    public Map<String, Object> getApolloConfig(@RequestParam String key) {
        apolloConfigExample.initApolloConfig();
        String value = apolloConfigExample.getProperty(key, "default");
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("key", key);
        result.put("value", value);
        return result;
    }

    @GetMapping("/config/nacos/get")
    public Map<String, Object> getNacosConfig() {
        nacosConfigExample.initNacosConfig();
        String content = nacosConfigExample.getConfig();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("content", content);
        return result;
    }
}