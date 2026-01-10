package com.architecture.admin;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaAdminExample {
    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminExample.class);
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        KafkaAdminExample example = new KafkaAdminExample();
        example.runAdminOperations();
    }

    public void runAdminOperations() {
        logger.info("Starting Kafka Admin operations");

        Properties props = createAdminProperties();
        
        try (AdminClient adminClient = AdminClient.create(props)) {
            
            createTopics(adminClient);
            
            listTopics(adminClient);
            
            describeTopics(adminClient);
            
            listConsumerGroups(adminClient);
            
            describeCluster(adminClient);
            
            getTopicConfigurations(adminClient);
            
            deleteTopics(adminClient);
            
        } catch (Exception e) {
            logger.error("Error in admin operations", e);
        }

        logger.info("Kafka Admin operations completed");
    }

    private void createTopics(AdminClient adminClient) {
        logger.info("Creating topics");
        
        List<NewTopic> topics = Arrays.asList(
            new NewTopic("admin-test-topic-1", 3, (short) 1),
            new NewTopic("admin-test-topic-2", 6, (short) 1)
                .configs(Map.of("cleanup.policy", "compact", "retention.ms", "86400000")),
            new NewTopic("admin-test-topic-3", 1, (short) 1)
        );

        CreateTopicsResult result = adminClient.createTopics(topics);
        
        result.values().forEach((topicName, future) -> {
            try {
                future.get();
                logger.info("Successfully created topic: {}", topicName);
            } catch (InterruptedException | ExecutionException e) {
                if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                    logger.info("Topic {} already exists", topicName);
                } else {
                    logger.error("Failed to create topic: {}", topicName, e);
                }
            }
        });
    }

    private void listTopics(AdminClient adminClient) {
        logger.info("Listing topics");
        
        try {
            ListTopicsResult result = adminClient.listTopics();
            Set<String> topicNames = result.names().get();
            
            logger.info("Found {} topics:", topicNames.size());
            topicNames.forEach(name -> logger.info("  - {}", name));
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error listing topics", e);
        }
    }

    private void describeTopics(AdminClient adminClient) {
        logger.info("Describing topics");
        
        try {
            Set<String> topicNames = Set.of("admin-test-topic-1", "admin-test-topic-2");
            DescribeTopicsResult result = adminClient.describeTopics(topicNames);
            
            Map<String, TopicDescription> descriptions = result.all().get();
            
            descriptions.forEach((name, description) -> {
                logger.info("Topic: {}", name);
                logger.info("  Partitions: {}", description.partitions().size());
                logger.info("  Internal: {}", description.isInternal());
                
                description.partitions().forEach(partition -> {
                    logger.info("    Partition {}: Leader={}, Replicas={}, ISR={}", 
                              partition.partition(), 
                              partition.leader().id(),
                              partition.replicas().size(),
                              partition.isr().size());
                });
            });
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error describing topics", e);
        }
    }

    private void listConsumerGroups(AdminClient adminClient) {
        logger.info("Listing consumer groups");
        
        try {
            ListConsumerGroupsResult result = adminClient.listConsumerGroups();
            Collection<ConsumerGroupListing> groups = result.all().get();
            
            logger.info("Found {} consumer groups:", groups.size());
            groups.forEach(group -> 
                logger.info("  - Group: {}, State: {}", group.groupId(), group.state()));
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error listing consumer groups", e);
        }
    }

    private void describeCluster(AdminClient adminClient) {
        logger.info("Describing cluster");
        
        try {
            DescribeClusterResult result = adminClient.describeCluster();
            
            String clusterId = result.clusterId().get();
            Collection<org.apache.kafka.common.Node> nodes = result.nodes().get();
            org.apache.kafka.common.Node controller = result.controller().get();
            
            logger.info("Cluster ID: {}", clusterId);
            logger.info("Controller: {} ({}:{})", controller.id(), controller.host(), controller.port());
            logger.info("Nodes: {}", nodes.size());
            
            nodes.forEach(node -> 
                logger.info("  - Node {}: {}:{}", node.id(), node.host(), node.port()));
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error describing cluster", e);
        }
    }

    private void getTopicConfigurations(AdminClient adminClient) {
        logger.info("Getting topic configurations");
        
        try {
            Set<ConfigResource> resources = Set.of(
                new ConfigResource(ConfigResource.Type.TOPIC, "admin-test-topic-1"),
                new ConfigResource(ConfigResource.Type.TOPIC, "admin-test-topic-2")
            );
            
            DescribeConfigsResult result = adminClient.describeConfigs(resources);
            Map<ConfigResource, Config> configs = result.all().get();
            
            configs.forEach((resource, config) -> {
                logger.info("Configuration for {}: {}", resource.name(), resource.type());
                config.entries().forEach(entry -> {
                    if (!entry.isDefault()) {
                        logger.info("  {}: {} (source: {})", 
                                  entry.name(), entry.value(), entry.source());
                    }
                });
            });
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error getting topic configurations", e);
        }
    }

    private void deleteTopics(AdminClient adminClient) {
        logger.info("Deleting test topics");
        
        Set<String> topicsToDelete = Set.of(
            "admin-test-topic-1", 
            "admin-test-topic-2", 
            "admin-test-topic-3"
        );
        
        DeleteTopicsResult result = adminClient.deleteTopics(topicsToDelete);
        
        result.values().forEach((topicName, future) -> {
            try {
                future.get();
                logger.info("Successfully deleted topic: {}", topicName);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Failed to delete topic: {}", topicName, e);
            }
        });
    }

    private Properties createAdminProperties() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);
        return props;
    }
}