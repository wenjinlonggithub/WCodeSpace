package com.architecture.algorithm.opensource;

/**
 * 开源项目中算法应用案例主类
 * 演示Java开源项目中经典算法的实际应用和核心原理
 */
public class OpenSourceAlgorithmApplications {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    Java开源项目中算法应用案例演示");
        System.out.println("===========================================");
        
        // 演示各种开源项目中的算法应用
        demonstrateSpringAlgorithms();
        demonstrateMyBatisAlgorithms();
        demonstrateNettyAlgorithms();
        demonstrateElasticsearchAlgorithms();
        demonstrateKafkaAlgorithms();
        demonstrateGuavaAlgorithms();
        demonstrateJacksonAlgorithms();
        
        System.out.println("\n===========================================");
        System.out.println("    演示完成！感谢使用开源算法应用案例库");
        System.out.println("===========================================");
    }
    
    private static void demonstrateSpringAlgorithms() {
        System.out.println("\n【Spring框架中的算法应用】");
        SpringAlgorithms springAlgorithms = new SpringAlgorithms();
        springAlgorithms.demonstrate();
    }
    
    private static void demonstrateMyBatisAlgorithms() {
        System.out.println("\n【MyBatis中的算法应用】");
        MyBatisAlgorithms myBatisAlgorithms = new MyBatisAlgorithms();
        myBatisAlgorithms.demonstrate();
    }
    
    private static void demonstrateNettyAlgorithms() {
        System.out.println("\n【Netty中的算法应用】");
        NettyAlgorithms nettyAlgorithms = new NettyAlgorithms();
        nettyAlgorithms.demonstrate();
    }
    
    private static void demonstrateElasticsearchAlgorithms() {
        System.out.println("\n【Elasticsearch中的算法应用】");
        ElasticsearchAlgorithms elasticsearchAlgorithms = new ElasticsearchAlgorithms();
        elasticsearchAlgorithms.demonstrate();
    }
    
    private static void demonstrateKafkaAlgorithms() {
        System.out.println("\n【Kafka中的算法应用】");
        KafkaAlgorithms kafkaAlgorithms = new KafkaAlgorithms();
        kafkaAlgorithms.demonstrate();
    }
    
    private static void demonstrateGuavaAlgorithms() {
        System.out.println("\n【Guava中的算法应用】");
        GuavaAlgorithms guavaAlgorithms = new GuavaAlgorithms();
        guavaAlgorithms.demonstrate();
    }
    
    private static void demonstrateJacksonAlgorithms() {
        System.out.println("\n【Jackson中的算法应用】");
        JacksonAlgorithms jacksonAlgorithms = new JacksonAlgorithms();
        jacksonAlgorithms.demonstrate();
    }
}