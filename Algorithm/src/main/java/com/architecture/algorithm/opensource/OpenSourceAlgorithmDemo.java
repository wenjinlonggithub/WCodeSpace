package com.architecture.algorithm.opensource;

/**
 * 开源项目算法应用演示类
 * 综合演示Java开源项目中各种算法的应用和核心原理
 */
public class OpenSourceAlgorithmDemo {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    Java开源项目算法应用案例演示");
        System.out.println("===========================================");
        System.out.println("本演示展示了多个知名Java开源项目中的核心算法");
        System.out.println("包括Spring、MyBatis、Netty、Elasticsearch等");
        System.out.println("===========================================");
        
        // 演示所有开源项目算法应用
        demonstrateAllOpenSourceAlgorithms();
        
        System.out.println("\n===========================================");
        System.out.println("演示完成！以上展示了主流Java开源项目的核心算法实现原理");
        System.out.println("这些算法在实际项目中被广泛应用，是学习算法应用的经典案例");
        System.out.println("===========================================");
    }
    
    private static void demonstrateAllOpenSourceAlgorithms() {
        // 1. Spring框架算法应用
        System.out.println("\n🔍 1. Spring框架算法应用演示");
        System.out.println("   " + "=".repeat(50));
        SpringAlgorithms springAlgorithms = new SpringAlgorithms();
        springAlgorithms.demonstrate();
        
        // 2. MyBatis框架算法应用
        System.out.println("\n🔍 2. MyBatis框架算法应用演示");
        System.out.println("   " + "=".repeat(50));
        MyBatisAlgorithms myBatisAlgorithms = new MyBatisAlgorithms();
        myBatisAlgorithms.demonstrate();
        
        // 3. Netty框架算法应用
        System.out.println("\n🔍 3. Netty框架算法应用演示");
        System.out.println("   " + "=".repeat(50));
        NettyAlgorithms nettyAlgorithms = new NettyAlgorithms();
        nettyAlgorithms.demonstrate();
        
        // 4. Elasticsearch算法应用
        System.out.println("\n🔍 4. Elasticsearch算法应用演示");
        System.out.println("   " + "=".repeat(50));
        ElasticsearchAlgorithms esAlgorithms = new ElasticsearchAlgorithms();
        esAlgorithms.demonstrate();
        
        // 5. Kafka算法应用
        System.out.println("\n🔍 5. Apache Kafka算法应用演示");
        System.out.println("   " + "=".repeat(50));
        KafkaAlgorithms kafkaAlgorithms = new KafkaAlgorithms();
        kafkaAlgorithms.demonstrate();
        
        // 6. Guava算法应用
        System.out.println("\n🔍 6. Google Guava算法应用演示");
        System.out.println("   " + "=".repeat(50));
        GuavaAlgorithms guavaAlgorithms = new GuavaAlgorithms();
        guavaAlgorithms.demonstrate();
        
        // 7. Jackson算法应用
        System.out.println("\n🔍 7. Jackson算法应用演示");
        System.out.println("   " + "=".repeat(50));
        JacksonAlgorithms jacksonAlgorithms = new JacksonAlgorithms();
        jacksonAlgorithms.demonstrate();
    }
}