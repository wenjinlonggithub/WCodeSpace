package com.architecture.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ECommerceExample {
    private static final Logger logger = LoggerFactory.getLogger(ECommerceExample.class);
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String ORDER_TOPIC = "ecommerce-orders";
    private static final String INVENTORY_TOPIC = "inventory-updates";
    private static final String PAYMENT_TOPIC = "payment-events";
    private static final String NOTIFICATION_TOPIC = "user-notifications";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public static void main(String[] args) {
        ECommerceExample example = new ECommerceExample();
        example.runECommerceSimulation();
    }

    public void runECommerceSimulation() {
        logger.info("Starting E-commerce Kafka simulation");
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        executor.submit(this::runOrderProducer);
        executor.submit(this::runInventoryConsumer);
        executor.submit(this::runPaymentProcessor);
        executor.submit(this::runNotificationService);
        
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        logger.info("E-commerce simulation completed");
    }

    private void runOrderProducer() {
        logger.info("Starting order producer");
        
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 50; i++) {
                Order order = createRandomOrder(i);
                String key = order.getOrderId();
                String value = objectMapper.writeValueAsString(order);
                
                ProducerRecord<String, String> record = new ProducerRecord<>(ORDER_TOPIC, key, value);
                
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        logger.info("Order created: {} - Amount: ${}", order.getOrderId(), order.getTotalAmount());
                        
                        sendInventoryUpdate(order);
                        sendPaymentEvent(order);
                    } else {
                        logger.error("Failed to create order: {}", order.getOrderId(), exception);
                    }
                });
                
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Error in order producer", e);
        }
    }

    private void sendInventoryUpdate(Order order) {
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (OrderItem item : order.getItems()) {
                InventoryUpdate update = new InventoryUpdate();
                update.setProductId(item.getProductId());
                update.setQuantityChange(-item.getQuantity());
                update.setOrderId(order.getOrderId());
                update.setTimestamp(System.currentTimeMillis());
                
                String key = item.getProductId();
                String value = objectMapper.writeValueAsString(update);
                
                ProducerRecord<String, String> record = new ProducerRecord<>(INVENTORY_TOPIC, key, value);
                producer.send(record);
            }
        } catch (Exception e) {
            logger.error("Error sending inventory update", e);
        }
    }

    private void sendPaymentEvent(Order order) {
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            PaymentEvent payment = new PaymentEvent();
            payment.setOrderId(order.getOrderId());
            payment.setUserId(order.getUserId());
            payment.setAmount(order.getTotalAmount());
            payment.setPaymentMethod("CREDIT_CARD");
            payment.setStatus("PENDING");
            payment.setTimestamp(System.currentTimeMillis());
            
            String key = order.getOrderId();
            String value = objectMapper.writeValueAsString(payment);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(PAYMENT_TOPIC, key, value);
            producer.send(record);
        } catch (Exception e) {
            logger.error("Error sending payment event", e);
        }
    }

    private void runInventoryConsumer() {
        logger.info("Starting inventory consumer");
        
        Properties props = createConsumerProperties("inventory-service");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(INVENTORY_TOPIC));
            
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        InventoryUpdate update = objectMapper.readValue(record.value(), InventoryUpdate.class);
                        processInventoryUpdate(update);
                    } catch (Exception e) {
                        logger.error("Error processing inventory update", e);
                    }
                }
                
                if (records.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in inventory consumer", e);
        }
    }

    private void processInventoryUpdate(InventoryUpdate update) {
        logger.info("Processing inventory update: Product {} - Change: {} for Order {}", 
                  update.getProductId(), update.getQuantityChange(), update.getOrderId());
        
        int currentStock = random.nextInt(100) + 50;
        int newStock = currentStock + update.getQuantityChange();
        
        if (newStock < 10) {
            logger.warn("Low stock alert for product {}: {} units remaining", 
                       update.getProductId(), newStock);
            
            sendLowStockNotification(update.getProductId(), newStock);
        }
    }

    private void runPaymentProcessor() {
        logger.info("Starting payment processor");
        
        Properties props = createConsumerProperties("payment-service");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(PAYMENT_TOPIC));
            
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        PaymentEvent payment = objectMapper.readValue(record.value(), PaymentEvent.class);
                        processPayment(payment);
                    } catch (Exception e) {
                        logger.error("Error processing payment", e);
                    }
                }
                
                if (records.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in payment processor", e);
        }
    }

    private void processPayment(PaymentEvent payment) {
        logger.info("Processing payment: Order {} - Amount: ${}", 
                  payment.getOrderId(), payment.getAmount());
        
        try {
            Thread.sleep(random.nextInt(2000) + 1000);
            
            boolean success = random.nextDouble() > 0.1;
            String status = success ? "COMPLETED" : "FAILED";
            
            payment.setStatus(status);
            payment.setTimestamp(System.currentTimeMillis());
            
            sendPaymentNotification(payment);
            
            logger.info("Payment {} for order {}: {}", 
                       payment.getOrderId(), payment.getOrderId(), status);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void runNotificationService() {
        logger.info("Starting notification service");
        
        Properties props = createConsumerProperties("notification-service");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(NOTIFICATION_TOPIC));
            
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        Notification notification = objectMapper.readValue(record.value(), Notification.class);
                        sendNotification(notification);
                    } catch (Exception e) {
                        logger.error("Error processing notification", e);
                    }
                }
                
                if (records.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in notification service", e);
        }
    }

    private void sendLowStockNotification(String productId, int stock) {
        try {
            Notification notification = new Notification();
            notification.setType("LOW_STOCK_ALERT");
            notification.setRecipient("inventory-manager@company.com");
            notification.setSubject("Low Stock Alert");
            notification.setMessage(String.format("Product %s has low stock: %d units remaining", productId, stock));
            notification.setTimestamp(System.currentTimeMillis());
            
            sendNotificationMessage(notification);
        } catch (Exception e) {
            logger.error("Error sending low stock notification", e);
        }
    }

    private void sendPaymentNotification(PaymentEvent payment) {
        try {
            Notification notification = new Notification();
            notification.setType("PAYMENT_UPDATE");
            notification.setRecipient(payment.getUserId());
            notification.setSubject("Payment Update");
            notification.setMessage(String.format("Payment for order %s is %s", 
                                                 payment.getOrderId(), payment.getStatus()));
            notification.setTimestamp(System.currentTimeMillis());
            
            sendNotificationMessage(notification);
        } catch (Exception e) {
            logger.error("Error sending payment notification", e);
        }
    }

    private void sendNotificationMessage(Notification notification) {
        Properties props = createProducerProperties();
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            String key = notification.getRecipient();
            String value = objectMapper.writeValueAsString(notification);
            
            ProducerRecord<String, String> record = new ProducerRecord<>(NOTIFICATION_TOPIC, key, value);
            producer.send(record);
        } catch (Exception e) {
            logger.error("Error sending notification message", e);
        }
    }

    private void sendNotification(Notification notification) {
        logger.info("Sending {} notification to {}: {}", 
                  notification.getType(), notification.getRecipient(), notification.getSubject());
    }

    private Order createRandomOrder(int id) {
        Order order = new Order();
        order.setOrderId("ORDER-" + String.format("%05d", id));
        order.setUserId("user-" + random.nextInt(100));
        order.setTimestamp(System.currentTimeMillis());
        
        String[] products = {"laptop", "phone", "tablet", "headphones", "keyboard", "mouse", "monitor", "speaker"};
        int itemCount = random.nextInt(3) + 1;
        
        double totalAmount = 0;
        OrderItem[] items = new OrderItem[itemCount];
        
        for (int i = 0; i < itemCount; i++) {
            OrderItem item = new OrderItem();
            item.setProductId(products[random.nextInt(products.length)]);
            item.setQuantity(random.nextInt(3) + 1);
            item.setPrice(random.nextDouble() * 500 + 50);
            
            items[i] = item;
            totalAmount += item.getPrice() * item.getQuantity();
        }
        
        order.setItems(items);
        order.setTotalAmount(totalAmount);
        
        return order;
    }

    private Properties createProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return props;
    }

    private Properties createConsumerProperties(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    public static class Order {
        private String orderId;
        private String userId;
        private OrderItem[] items;
        private double totalAmount;
        private long timestamp;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public OrderItem[] getItems() { return items; }
        public void setItems(OrderItem[] items) { this.items = items; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class OrderItem {
        private String productId;
        private int quantity;
        private double price;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public static class InventoryUpdate {
        private String productId;
        private int quantityChange;
        private String orderId;
        private long timestamp;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public int getQuantityChange() { return quantityChange; }
        public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class PaymentEvent {
        private String orderId;
        private String userId;
        private double amount;
        private String paymentMethod;
        private String status;
        private long timestamp;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public static class Notification {
        private String type;
        private String recipient;
        private String subject;
        private String message;
        private long timestamp;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}