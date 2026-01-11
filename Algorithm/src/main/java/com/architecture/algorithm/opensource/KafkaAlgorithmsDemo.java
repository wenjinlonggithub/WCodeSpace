package com.architecture.algorithm.opensource;

/**
 * Kafka算法应用案例演示类
 */
public class KafkaAlgorithmsDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Kafka算法应用案例演示 ===\n");
        
        KafkaAlgorithms kafkaAlgorithms = new KafkaAlgorithms();
        kafkaAlgorithms.demonstrate();
        
        System.out.println("\n=== 一致性哈希算法专项演示 ===\n");
        
        ConsistentHashDemo.demonstrateConsistentHashing();
    }
}