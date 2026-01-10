package com.architecture;

import com.architecture.admin.KafkaAdminExample;
import com.architecture.config.KafkaConfigurationExample;
import com.architecture.consumer.JsonConsumerExample;
import com.architecture.consumer.KafkaConsumerExample;
import com.architecture.examples.ECommerceExample;
import com.architecture.producer.JsonProducerExample;
import com.architecture.producer.KafkaProducerExample;
import com.architecture.streams.UserEventStreamsExample;
import com.architecture.streams.WordCountStreamsExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("=== Apache Kafka 完整实现示例 ===");
        
        if (args.length == 0) {
            showUsage();
            return;
        }
        
        String example = args[0].toLowerCase();
        
        try {
            switch (example) {
                case "producer":
                    logger.info("运行 Kafka Producer 示例");
                    KafkaProducerExample.main(new String[]{});
                    break;
                    
                case "json-producer":
                    logger.info("运行 JSON Producer 示例");
                    JsonProducerExample.main(new String[]{});
                    break;
                    
                case "consumer":
                    logger.info("运行 Kafka Consumer 示例");
                    KafkaConsumerExample.main(new String[]{});
                    break;
                    
                case "json-consumer":
                    logger.info("运行 JSON Consumer 示例");
                    JsonConsumerExample.main(new String[]{});
                    break;
                    
                case "streams-wordcount":
                    logger.info("运行 Kafka Streams 词频统计示例");
                    WordCountStreamsExample.main(new String[]{});
                    break;
                    
                case "streams-events":
                    logger.info("运行 Kafka Streams 用户事件处理示例");
                    UserEventStreamsExample.main(new String[]{});
                    break;
                    
                case "admin":
                    logger.info("运行 Kafka Admin 示例");
                    KafkaAdminExample.main(new String[]{});
                    break;
                    
                case "config":
                    logger.info("运行 Kafka 配置示例");
                    KafkaConfigurationExample.main(new String[]{});
                    break;
                    
                case "ecommerce":
                    logger.info("运行电商系统示例");
                    ECommerceExample.main(new String[]{});
                    break;
                    
                default:
                    logger.error("未知示例: {}", example);
                    showUsage();
            }
        } catch (Exception e) {
            logger.error("运行示例时发生错误: {}", example, e);
        }
    }
    
    private static void showUsage() {
        System.out.println("\n=== Apache Kafka 完整实现示例 ===");
        System.out.println("使用方法: java -jar kafka.jar <example>");
        System.out.println("\n可用示例:");
        System.out.println("  producer          - Kafka Producer 基础示例");
        System.out.println("  json-producer     - JSON 格式 Producer 示例");
        System.out.println("  consumer          - Kafka Consumer 基础示例");
        System.out.println("  json-consumer     - JSON 格式 Consumer 示例");
        System.out.println("  streams-wordcount - Kafka Streams 词频统计");
        System.out.println("  streams-events    - Kafka Streams 事件处理");
        System.out.println("  admin             - Kafka Admin 管理操作");
        System.out.println("  config            - Kafka 配置示例");
        System.out.println("  ecommerce         - 电商系统完整示例");
        System.out.println("\n示例:");
        System.out.println("  java -jar kafka.jar producer");
        System.out.println("  java -jar kafka.jar ecommerce");
        System.out.println("\n更多信息请查看 README.md 和 INTERVIEW.md 文件");
    }
}
