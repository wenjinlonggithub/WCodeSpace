package com.architecture.example;

import com.architecture.engine.*;
import com.architecture.index.BPlusTree;
import java.util.*;
import java.util.concurrent.*;

/**
 * MySQL核心概念综合演示
 * 通过实际案例演示MySQL的关键特性和工作原理：
 * 1. 事务ACID特性演示
 * 2. 隔离级别对比
 * 3. 死锁检测和处理
 * 4. 索引优化案例
 * 5. MVCC并发控制
 * 6. 查询优化实例
 * 7. 存储引擎特性
 */
public class MySQLConceptsDemo {
    
    private final InnoDB innodb;
    private final MVCCEngine mvccEngine;
    private final QueryExecutor queryExecutor;
    
    public MySQLConceptsDemo() {
        this.innodb = new InnoDB();
        this.mvccEngine = new MVCCEngine();
        this.queryExecutor = new QueryExecutor();
    }
    
    /**
     * 演示1：事务ACID特性
     */
    public void demonstrateACIDProperties() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 演示1：事务ACID特性");
        System.out.println("=".repeat(60));
        
        // Atomicity (原子性) - 要么全部成功，要么全部失败
        System.out.println("\n🔬 原子性演示：银行转账");
        demonstrateAtomicity();
        
        // Consistency (一致性) - 数据库从一个一致性状态转换到另一个一致性状态  
        System.out.println("\n🔬 一致性演示：库存管理");
        demonstrateConsistency();
        
        // Isolation (隔离性) - 并发事务之间的隔离
        System.out.println("\n🔬 隔离性演示：并发读写");
        demonstrateIsolation();
        
        // Durability (持久性) - 已提交的事务对数据的修改是永久的
        System.out.println("\n🔬 持久性演示：数据恢复");
        demonstrateDurability();
    }
    
    /**
     * 原子性演示
     */
    private void demonstrateAtomicity() {
        TransactionManager tm = innodb.getTransactionManager();
        InnoDB.LogManager logManager = innodb.getLogManager();
        
        // 模拟银行账户
        Map<String, Integer> accounts = new HashMap<>();
        accounts.put("account_1", 1000);
        accounts.put("account_2", 500);
        
        System.out.println("初始账户余额:");
        System.out.println("  账户1: " + accounts.get("account_1"));
        System.out.println("  账户2: " + accounts.get("account_2"));
        
        // 开始转账事务
        TransactionManager.Transaction txn = tm.beginTransaction(
            TransactionManager.IsolationLevel.READ_COMMITTED);
        
        try {
            // 从账户1扣款300
            int balance1 = accounts.get("account_1");
            if (balance1 >= 300) {
                accounts.put("account_1", balance1 - 300);
                tm.recordOperation(txn.getTransactionId(), "accounts", "UPDATE",
                    Map.of("account", "account_1", "balance", balance1),
                    Map.of("account", "account_1", "balance", balance1 - 300));
                
                // 向账户2存款300
                int balance2 = accounts.get("account_2");
                accounts.put("account_2", balance2 + 300);
                tm.recordOperation(txn.getTransactionId(), "accounts", "UPDATE",
                    Map.of("account", "account_2", "balance", balance2),
                    Map.of("account", "account_2", "balance", balance2 + 300));
                
                // 模拟转账失败场景
                if (Math.random() > 0.7) {
                    throw new RuntimeException("网络错误，转账失败");
                }
                
                // 提交事务
                tm.commitTransaction(txn.getTransactionId());
                System.out.println("✅ 转账成功");
                
            } else {
                throw new RuntimeException("余额不足");
            }
            
        } catch (Exception e) {
            // 回滚事务，恢复原始状态
            tm.rollbackTransaction(txn.getTransactionId());
            accounts.put("account_1", 1000);
            accounts.put("account_2", 500);
            System.out.printf("❌ 转账失败，事务回滚: %s%n", e.getMessage());
        }
        
        System.out.println("最终账户余额:");
        System.out.println("  账户1: " + accounts.get("account_1"));
        System.out.println("  账户2: " + accounts.get("account_2"));
        System.out.println("📝 原子性保证：转账要么完全成功，要么完全失败");
    }
    
    /**
     * 一致性演示
     */
    private void demonstrateConsistency() {
        System.out.println("模拟商品库存管理系统");
        
        // 定义一致性约束：库存不能为负数
        class InventoryManager {
            private Map<String, Integer> inventory = new HashMap<>();
            
            public InventoryManager() {
                inventory.put("商品A", 100);
                inventory.put("商品B", 50);
            }
            
            public boolean orderProduct(String product, int quantity) {
                int currentStock = inventory.getOrDefault(product, 0);
                System.out.printf("尝试订购 %s %d 件，当前库存: %d%n", product, quantity, currentStock);
                
                if (currentStock >= quantity) {
                    inventory.put(product, currentStock - quantity);
                    System.out.printf("✅ 订购成功，剩余库存: %d%n", inventory.get(product));
                    return true;
                } else {
                    System.out.println("❌ 库存不足，订购失败");
                    return false;
                }
            }
            
            public void showInventory() {
                System.out.println("当前库存状态:");
                inventory.forEach((product, stock) -> 
                    System.out.printf("  %s: %d 件%n", product, stock));
            }
        }
        
        InventoryManager manager = new InventoryManager();
        manager.showInventory();
        
        // 正常订购
        manager.orderProduct("商品A", 30);
        
        // 超量订购（违反一致性约束）
        manager.orderProduct("商品B", 60);
        
        manager.showInventory();
        System.out.println("📝 一致性保证：系统始终满足业务规则（库存不为负数）");
    }
    
    /**
     * 隔离性演示
     */
    private void demonstrateIsolation() {
        System.out.println("模拟并发事务的隔离性");
        
        // 使用MVCC引擎演示不同隔离级别
        MVCCEngine.Transaction txn1 = mvccEngine.beginTransaction(
            MVCCEngine.IsolationLevel.READ_COMMITTED);
        MVCCEngine.Transaction txn2 = mvccEngine.beginTransaction(
            MVCCEngine.IsolationLevel.REPEATABLE_READ);
        
        // 插入初始数据
        mvccEngine.insert(txn1.getTransactionId(), "product:1", 
            Map.of("id", 1, "name", "商品1", "price", 100));
        mvccEngine.commitTransaction(txn1.getTransactionId());
        
        // 重新开始事务
        txn1 = mvccEngine.beginTransaction(MVCCEngine.IsolationLevel.READ_COMMITTED);
        txn2 = mvccEngine.beginTransaction(MVCCEngine.IsolationLevel.REPEATABLE_READ);
        
        // 事务2先读取数据
        Map<String, Object> data2First = mvccEngine.snapshotRead(txn2.getTransactionId(), "product:1");
        System.out.println("事务2第一次读取: " + data2First);
        
        // 事务1修改数据
        mvccEngine.update(txn1.getTransactionId(), "product:1",
            Map.of("id", 1, "name", "商品1", "price", 120));
        mvccEngine.commitTransaction(txn1.getTransactionId());
        
        // 事务2再次读取数据
        Map<String, Object> data2Second = mvccEngine.snapshotRead(txn2.getTransactionId(), "product:1");
        System.out.println("事务2第二次读取: " + data2Second);
        
        mvccEngine.commitTransaction(txn2.getTransactionId());
        
        boolean isRepeatable = Objects.equals(data2First.get("price"), data2Second.get("price"));
        System.out.printf("📝 隔离性验证：可重复读 = %s%n", isRepeatable);
    }
    
    /**
     * 持久性演示
     */
    private void demonstrateDurability() {
        InnoDB.LogManager logManager = innodb.getLogManager();
        
        System.out.println("模拟系统崩溃后的数据恢复");
        
        // 记录操作日志
        long txnId = 1001;
        logManager.writeBeginLog(txnId);
        logManager.writeLog(txnId, InnoDB.LogManager.LogType.INSERT, "users",
            Collections.emptyMap(), Map.of("id", 1, "name", "张三"));
        logManager.writeLog(txnId, InnoDB.LogManager.LogType.UPDATE, "users",
            Map.of("id", 1, "name", "张三"), Map.of("id", 1, "name", "张三丰"));
        logManager.writeCommitLog(txnId);
        
        System.out.println("📄 事务已提交，日志已写入");
        System.out.println("日志统计: " + logManager.getStats());
        
        // 模拟系统崩溃和恢复
        System.out.println("💥 模拟系统崩溃...");
        System.out.println("🔄 系统重启，开始恢复...");
        
        logManager.recovery();
        System.out.println("📝 持久性保证：已提交的数据即使在系统崩溃后也能恢复");
    }
    
    /**
     * 演示2：隔离级别对比
     */
    public void demonstrateIsolationLevels() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔒 演示2：MySQL隔离级别对比");
        System.out.println("=".repeat(60));
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);
        
        // 测试不同隔离级别的行为
        MVCCEngine.IsolationLevel[] levels = {
            MVCCEngine.IsolationLevel.READ_UNCOMMITTED,
            MVCCEngine.IsolationLevel.READ_COMMITTED,
            MVCCEngine.IsolationLevel.REPEATABLE_READ,
            MVCCEngine.IsolationLevel.SERIALIZABLE
        };
        
        // 准备测试数据
        MVCCEngine.Transaction setupTxn = mvccEngine.beginTransaction(
            MVCCEngine.IsolationLevel.READ_COMMITTED);
        mvccEngine.insert(setupTxn.getTransactionId(), "test:1", 
            Map.of("id", 1, "value", 100));
        mvccEngine.commitTransaction(setupTxn.getTransactionId());
        
        for (MVCCEngine.IsolationLevel level : levels) {
            executor.submit(() -> {
                try {
                    testIsolationLevel(level);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
        
        System.out.println("\n📝 隔离级别总结:");
        System.out.println("  READ_UNCOMMITTED: 可能出现脏读、不可重复读、幻读");
        System.out.println("  READ_COMMITTED:   可能出现不可重复读、幻读");
        System.out.println("  REPEATABLE_READ:  可能出现幻读");
        System.out.println("  SERIALIZABLE:     无并发问题，但性能最低");
    }
    
    /**
     * 测试特定隔离级别
     */
    private void testIsolationLevel(MVCCEngine.IsolationLevel level) {
        System.out.printf("\n🧪 测试隔离级别: %s%n", level);
        
        MVCCEngine.Transaction txn1 = mvccEngine.beginTransaction(level);
        MVCCEngine.Transaction txn2 = mvccEngine.beginTransaction(level);
        
        try {
            // 事务1读取初始数据
            Map<String, Object> data1 = mvccEngine.snapshotRead(txn1.getTransactionId(), "test:1");
            System.out.printf("  事务1第一次读取: %s%n", data1);
            
            // 事务2修改数据
            mvccEngine.update(txn2.getTransactionId(), "test:1", 
                Map.of("id", 1, "value", 200));
            
            // 事务1再次读取（测试是否能看到未提交的修改）
            Map<String, Object> data2 = mvccEngine.snapshotRead(txn1.getTransactionId(), "test:1");
            System.out.printf("  事务1第二次读取（事务2未提交): %s%n", data2);
            
            // 事务2提交
            mvccEngine.commitTransaction(txn2.getTransactionId());
            
            // 事务1第三次读取（测试可重复读）
            Map<String, Object> data3 = mvccEngine.snapshotRead(txn1.getTransactionId(), "test:1");
            System.out.printf("  事务1第三次读取（事务2已提交): %s%n", data3);
            
            mvccEngine.commitTransaction(txn1.getTransactionId());
            
        } catch (Exception e) {
            mvccEngine.rollbackTransaction(txn1.getTransactionId());
            mvccEngine.rollbackTransaction(txn2.getTransactionId());
            System.out.printf("  测试失败: %s%n", e.getMessage());
        }
    }
    
    /**
     * 演示3：死锁检测和处理
     */
    public void demonstrateDeadlockDetection() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("💀 演示3：死锁检测和处理");
        System.out.println("=".repeat(60));
        
        System.out.println("模拟经典的死锁场景：");
        System.out.println("  事务A：锁定资源1 → 请求资源2");
        System.out.println("  事务B：锁定资源2 → 请求资源1");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(2);
        CountDownLatch endLatch = new CountDownLatch(2);
        
        // 模拟死锁场景
        executor.submit(() -> {
            try {
                startLatch.countDown();
                startLatch.await(); // 等待两个线程都准备好
                simulateDeadlockTransaction("事务A", "resource1", "resource2");
            } catch (Exception e) {
                System.out.printf("事务A异常: %s%n", e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });
        
        executor.submit(() -> {
            try {
                startLatch.countDown();
                startLatch.await(); // 等待两个线程都准备好
                Thread.sleep(50); // 稍微延迟，确保交错执行
                simulateDeadlockTransaction("事务B", "resource2", "resource1");
            } catch (Exception e) {
                System.out.printf("事务B异常: %s%n", e.getMessage());
            } finally {
                endLatch.countDown();
            }
        });
        
        try {
            endLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
        
        System.out.println("\n📝 死锁处理策略:");
        System.out.println("  1. 死锁检测：周期性检测等待图中的环");
        System.out.println("  2. 死锁预防：按顺序获取锁");
        System.out.println("  3. 死锁避免：超时机制");
        System.out.println("  4. 死锁解除：回滚代价最小的事务");
    }
    
    /**
     * 模拟死锁事务
     */
    private void simulateDeadlockTransaction(String txnName, String resource1, String resource2) {
        TransactionManager tm = innodb.getTransactionManager();
        TransactionManager.Transaction txn = tm.beginTransaction(
            TransactionManager.IsolationLevel.READ_COMMITTED);
        
        try {
            System.out.printf("%s: 获取资源 %s%n", txnName, resource1);
            // 模拟获取第一个资源
            Thread.sleep(100);
            
            System.out.printf("%s: 尝试获取资源 %s%n", txnName, resource2);
            // 模拟获取第二个资源（可能产生死锁）
            Thread.sleep(100);
            
            System.out.printf("%s: 成功获取所有资源%n", txnName);
            tm.commitTransaction(txn.getTransactionId());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tm.rollbackTransaction(txn.getTransactionId());
        }
    }
    
    /**
     * 演示4：索引优化实战
     */
    public void demonstrateIndexOptimization() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📇 演示4：索引优化实战");
        System.out.println("=".repeat(60));
        
        // 创建B+树索引
        BPlusTree<Integer, String> primaryIndex = new BPlusTree<>(4);
        BPlusTree<Integer, String> ageIndex = new BPlusTree<>(4);
        
        System.out.println("📊 准备测试数据...");
        
        // 插入测试数据
        for (int i = 1; i <= 20; i++) {
            String userData = String.format("User%d:Age%d", i, 20 + (i % 30));
            primaryIndex.insert(i, userData);
            ageIndex.insert(20 + (i % 30), userData);
        }
        
        System.out.println("✅ 数据插入完成");
        System.out.println("主键索引统计: " + primaryIndex.getStats());
        System.out.println("年龄索引统计: " + ageIndex.getStats());
        
        // 查询优化演示
        System.out.println("\n🔍 查询优化对比:");
        
        // 1. 主键查询（最优）
        System.out.println("\n1️⃣ 主键查询 (id = 10):");
        long start = System.nanoTime();
        String result1 = primaryIndex.search(10);
        long time1 = System.nanoTime() - start;
        System.out.printf("结果: %s%n", result1);
        System.out.printf("查询时间: %.2f μs%n", time1 / 1000.0);
        
        // 2. 索引范围查询
        System.out.println("\n2️⃣ 年龄范围查询 (age BETWEEN 25 AND 35):");
        start = System.nanoTime();
        List<String> result2 = ageIndex.rangeQuery(25, 35);
        long time2 = System.nanoTime() - start;
        System.out.printf("结果数量: %d%n", result2.size());
        System.out.printf("查询时间: %.2f μs%n", time2 / 1000.0);
        
        // 3. 使用查询执行器进行复杂查询
        System.out.println("\n3️⃣ 复杂查询执行计划分析:");
        
        QueryExecutor.SQLQuery complexQuery = new QueryExecutor.SQLQuery.Builder()
            .select("users")
            .columns("id", "name", "age")
            .where("age", ">", 25)
            .where("name", "LIKE", "张")
            .orderBy("age")
            .limit(10)
            .build();
        
        queryExecutor.explainQuery(complexQuery);
        
        System.out.println("\n📝 索引优化建议:");
        System.out.println("  1. 为经常查询的列创建索引");
        System.out.println("  2. 复合索引的列顺序很重要");
        System.out.println("  3. 避免在索引列上使用函数");
        System.out.println("  4. 定期分析索引使用情况");
    }
    
    /**
     * 演示5：查询优化实例
     */
    public void demonstrateQueryOptimization() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧠 演示5：查询优化实例");
        System.out.println("=".repeat(60));
        
        System.out.println("演示MySQL查询优化器如何选择最优执行计划");
        
        // 创建不同类型的查询
        List<QueryExecutor.SQLQuery> queries = Arrays.asList(
            // 简单点查询
            new QueryExecutor.SQLQuery.Builder()
                .select("users")
                .columns("*")
                .where("id", "=", 1)
                .build(),
            
            // 范围查询
            new QueryExecutor.SQLQuery.Builder()
                .select("users")
                .columns("name", "age")
                .where("age", "BETWEEN", Arrays.asList(25, 35))
                .build(),
            
            // 复杂条件查询
            new QueryExecutor.SQLQuery.Builder()
                .select("orders")
                .columns("*")
                .where("status", "=", "ACTIVE")
                .where("amount", ">", 1000)
                .orderBy("create_time")
                .limit(100)
                .build()
        );
        
        for (int i = 0; i < queries.size(); i++) {
            QueryExecutor.SQLQuery query = queries.get(i);
            System.out.printf("\n📋 查询 %d: %s%n", i + 1, query);
            
            // 分析执行计划
            queryExecutor.explainQuery(query);
            
            // 执行查询
            QueryExecutor.QueryResult result = queryExecutor.executeQuery(query);
            System.out.printf("执行结果: %s%n", result.getMessage());
        }
        
        System.out.println("\n📝 查询优化技巧:");
        System.out.println("  1. 使用EXPLAIN分析执行计划");
        System.out.println("  2. 避免SELECT *，只查询需要的列");
        System.out.println("  3. 合理使用WHERE条件过滤数据");
        System.out.println("  4. 注意ORDER BY和LIMIT的组合使用");
        System.out.println("  5. 考虑查询缓存的使用");
    }
    
    /**
     * 演示6：并发控制综合案例
     */
    public void demonstrateConcurrencyControl() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔄 演示6：并发控制综合案例");
        System.out.println("=".repeat(60));
        
        System.out.println("模拟电商系统的并发订单处理");
        
        // 初始化商品库存
        mvccEngine.insert(1, "product:1", Map.of("id", 1, "name", "商品1", "stock", 100));
        mvccEngine.commitTransaction(1);
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        List<Future<String>> futures = new ArrayList<>();
        
        // 模拟5个并发订单
        for (int i = 1; i <= 5; i++) {
            final int orderId = i;
            final int orderQuantity = 15 + orderId * 5; // 不同的订单数量
            
            Future<String> future = executor.submit(() -> {
                try {
                    return processOrder(orderId, orderQuantity);
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }
        
        try {
            latch.await(10, TimeUnit.SECONDS);
            
            System.out.println("\n📋 所有订单处理结果:");
            for (int i = 0; i < futures.size(); i++) {
                try {
                    String result = futures.get(i).get(1, TimeUnit.SECONDS);
                    System.out.printf("  订单 %d: %s%n", i + 1, result);
                } catch (Exception e) {
                    System.out.printf("  订单 %d: 处理失败 - %s%n", i + 1, e.getMessage());
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
        
        // 显示最终库存
        MVCCEngine.Transaction checkTxn = mvccEngine.beginTransaction(
            MVCCEngine.IsolationLevel.READ_COMMITTED);
        Map<String, Object> finalStock = mvccEngine.snapshotRead(
            checkTxn.getTransactionId(), "product:1");
        mvccEngine.commitTransaction(checkTxn.getTransactionId());
        
        System.out.println("\n📦 最终库存状态: " + finalStock);
        System.out.println("\n📊 MVCC统计: " + mvccEngine.getStats());
    }
    
    /**
     * 处理单个订单
     */
    private String processOrder(int orderId, int quantity) {
        MVCCEngine.Transaction txn = mvccEngine.beginTransaction(
            MVCCEngine.IsolationLevel.REPEATABLE_READ);
        
        try {
            // 读取当前库存
            Map<String, Object> product = mvccEngine.snapshotRead(
                txn.getTransactionId(), "product:1");
            
            if (product == null) {
                return "商品不存在";
            }
            
            int currentStock = (Integer) product.get("stock");
            System.out.printf("订单 %d: 尝试购买 %d 件，当前库存 %d%n", 
                orderId, quantity, currentStock);
            
            if (currentStock >= quantity) {
                // 模拟处理时间
                Thread.sleep(100 + orderId * 10);
                
                // 更新库存
                Map<String, Object> newProduct = new HashMap<>(product);
                newProduct.put("stock", currentStock - quantity);
                
                mvccEngine.update(txn.getTransactionId(), "product:1", newProduct);
                mvccEngine.commitTransaction(txn.getTransactionId());
                
                return String.format("成功购买 %d 件，剩余库存 %d", 
                    quantity, currentStock - quantity);
            } else {
                mvccEngine.rollbackTransaction(txn.getTransactionId());
                return String.format("库存不足，需要 %d 件，仅有 %d 件", 
                    quantity, currentStock);
            }
            
        } catch (Exception e) {
            mvccEngine.rollbackTransaction(txn.getTransactionId());
            return "处理失败: " + e.getMessage();
        }
    }
    
    /**
     * 主演示方法
     */
    public static void demonstrateAllConcepts() {
        System.out.println("🎯 MySQL核心概念综合演示");
        System.out.println("=".repeat(80));
        System.out.println("本演示通过实际案例展示MySQL的核心特性和工作原理");
        
        MySQLConceptsDemo demo = new MySQLConceptsDemo();
        
        try {
            // 1. ACID特性演示
            demo.demonstrateACIDProperties();
            
            // 2. 隔离级别对比
            demo.demonstrateIsolationLevels();
            
            // 3. 死锁检测
            demo.demonstrateDeadlockDetection();
            
            // 4. 索引优化
            demo.demonstrateIndexOptimization();
            
            // 5. 查询优化
            demo.demonstrateQueryOptimization();
            
            // 6. 并发控制
            demo.demonstrateConcurrencyControl();
            
        } catch (Exception e) {
            System.err.printf("演示过程中发生错误: %s%n", e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ MySQL核心概念演示完成");
        System.out.println("=".repeat(80));
        
        System.out.println("\n📚 核心知识点总结:");
        System.out.println("  🔐 ACID特性：原子性、一致性、隔离性、持久性");
        System.out.println("  🔒 隔离级别：READ_UNCOMMITTED < READ_COMMITTED < REPEATABLE_READ < SERIALIZABLE");
        System.out.println("  💀 死锁处理：检测、预防、避免、解除");
        System.out.println("  📇 索引优化：B+树、复合索引、索引选择性");
        System.out.println("  🧠 查询优化：执行计划、成本估算、访问路径选择");
        System.out.println("  🔄 MVCC：多版本并发控制、读视图、版本链");
        System.out.println("  💾 存储引擎：InnoDB特性、缓冲池、日志管理");
    }
    
    public static void main(String[] args) {
        demonstrateAllConcepts();
    }
}