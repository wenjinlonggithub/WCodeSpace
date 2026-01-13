package com.architecture.datastructure.linear.array;

/**
 * DynamicArray 实际应用场景演示
 * DynamicArray Practical Use Case Demonstrations
 *
 * <p>应用场景:
 * <ol>
 *   <li>数据收集与批处理 - 收集不确定数量的数据</li>
 *   <li>缓存管理 - 存储最近访问的数据</li>
 *   <li>排行榜系统 - 动态维护排名列表</li>
 *   <li>任务批处理 - 收集任务后统一处理</li>
 *   <li>日志收集 - 收集日志条目</li>
 * </ol>
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class DynamicArrayDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   DynamicArray 实际应用演示                  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        // 场景1: 学生成绩管理系统
        demonstrateScoreManagement();

        // 场景2: 购物车系统
        demonstrateShoppingCart();

        // 场景3: 扩容机制演示
        demonstrateResizing();

        // 场景4: 性能对比
        demonstratePerformance();
    }

    /**
     * 场景1: 学生成绩管理系统
     * 使用动态数组管理不确定数量的学生成绩
     *
     * 实际应用: 教育系统、在线考试、成绩统计
     */
    private static void demonstrateScoreManagement() {
        System.out.println("【场景1: 学生成绩管理系统】");
        System.out.println("应用: 教育系统、在线考试、成绩统计\n");

        ScoreManager scoreManager = new ScoreManager();

        // 添加学生成绩
        System.out.println(">>> 添加学生成绩:");
        scoreManager.addScore("张三", 85);
        scoreManager.addScore("李四", 92);
        scoreManager.addScore("王五", 78);
        scoreManager.addScore("赵六", 95);
        scoreManager.addScore("孙七", 88);

        System.out.println("当前学生数: " + scoreManager.getStudentCount());
        System.out.println();

        // 显示所有成绩
        System.out.println(">>> 所有学生成绩:");
        scoreManager.displayAllScores();
        System.out.println();

        // 计算统计信息
        System.out.println(">>> 成绩统计:");
        System.out.println("平均分: " + scoreManager.getAverageScore());
        System.out.println("最高分: " + scoreManager.getMaxScore());
        System.out.println("最低分: " + scoreManager.getMinScore());
        System.out.println("及格人数: " + scoreManager.getPassCount());
        System.out.println();

        // 查找学生
        System.out.println(">>> 查找学生:");
        int index = scoreManager.findStudent("李四");
        if (index >= 0) {
            System.out.println("李四的成绩: " + scoreManager.getScore(index));
        }

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 场景2: 购物车系统
     * 使用动态数组管理购物车商品
     *
     * 实际应用: 电商网站、在线商城
     */
    private static void demonstrateShoppingCart() {
        System.out.println("【场景2: 购物车系统】");
        System.out.println("应用: 电商网站、在线商城\n");

        ShoppingCart cart = new ShoppingCart();

        // 添加商品
        System.out.println(">>> 添加商品到购物车:");
        cart.addItem("MacBook Pro", 12999.00, 1);
        cart.addItem("iPhone 15", 5999.00, 2);
        cart.addItem("AirPods Pro", 1999.00, 1);

        cart.displayCart();
        System.out.println();

        // 更新商品数量
        System.out.println(">>> 修改商品数量:");
        cart.updateQuantity(1, 3);  // 将iPhone数量改为3
        cart.displayCart();
        System.out.println();

        // 删除商品
        System.out.println(">>> 删除商品:");
        cart.removeItem(2);  // 删除AirPods
        cart.displayCart();
        System.out.println();

        // 结算
        System.out.println(">>> 购物车结算:");
        System.out.println("总金额: ¥" + cart.getTotalPrice());

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 场景3: 扩容机制演示
     * 展示动态数组的自动扩容过程
     */
    private static void demonstrateResizing() {
        System.out.println("【场景3: 动态扩容机制演示】");
        System.out.println("观察数组容量变化\n");

        // 创建初始容量为4的数组
        DynamicArrayImplementation<Integer> array = new DynamicArrayImplementation<>(4);

        System.out.println("初始容量: " + array.capacity() + ", 大小: " + array.size());

        // 逐步添加元素，观察扩容
        for (int i = 1; i <= 10; i++) {
            array.add(i);
            System.out.println("添加元素 " + i + " 后 -> 容量: " + array.capacity() +
                             ", 大小: " + array.size() +
                             ", 负载因子: " + String.format("%.2f", (double)array.size() / array.capacity()));
        }

        System.out.println("\n最终数组内容: " + array);

        System.out.println("\n扩容历史:");
        System.out.println("4 -> 8 -> 16 (容量翻倍策略)");

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 场景4: 性能对比演示
     * 对比不同操作的性能差异
     */
    private static void demonstratePerformance() {
        System.out.println("【场景4: 操作性能对比】");
        System.out.println();

        DynamicArrayImplementation<Integer> array = new DynamicArrayImplementation<>();

        // 准备测试数据
        int testSize = 10000;
        for (int i = 0; i < testSize; i++) {
            array.add(i);
        }

        // 测试1: 随机访问（快）
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            array.get(testSize / 2);
        }
        long randomAccessTime = System.nanoTime() - start;

        // 测试2: 尾部添加（快）
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            array.add(i);
        }
        long appendTime = System.nanoTime() - start;

        // 测试3: 中间插入（慢）
        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            array.add(testSize / 2, i);
        }
        long insertTime = System.nanoTime() - start;

        // 测试4: 查找元素（慢）
        start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            array.indexOf(testSize - 1);
        }
        long searchTime = System.nanoTime() - start;

        System.out.println("性能测试结果 (1000次操作):");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("随机访问:   " + String.format("%,d", randomAccessTime) + " ns  ⚡ 超快 (O(1))");
        System.out.println("尾部添加:   " + String.format("%,d", appendTime) + " ns  ⚡ 快 (O(1)均摊)");
        System.out.println("中间插入:   " + String.format("%,d", insertTime) + " ns  🐢 慢 (O(n))");
        System.out.println("查找元素:   " + String.format("%,d", searchTime) + " ns  🐢 慢 (O(n))");
        System.out.println();

        System.out.println("结论:");
        System.out.println("✓ 数组适合: 频繁随机访问、尾部添加的场景");
        System.out.println("✗ 数组不适合: 频繁中间插入、删除、查找的场景");

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    // ==================== 辅助类 ====================

    /**
     * 成绩管理器
     * 使用动态数组管理学生成绩
     */
    static class ScoreManager {
        private DynamicArrayImplementation<StudentScore> scores;

        public ScoreManager() {
            scores = new DynamicArrayImplementation<>();
        }

        public void addScore(String name, int score) {
            scores.add(new StudentScore(name, score));
        }

        public int getScore(int index) {
            return scores.get(index).score;
        }

        public int findStudent(String name) {
            for (int i = 0; i < scores.size(); i++) {
                if (scores.get(i).name.equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        public void displayAllScores() {
            for (int i = 0; i < scores.size(); i++) {
                StudentScore s = scores.get(i);
                System.out.println((i + 1) + ". " + s.name + ": " + s.score + "分");
            }
        }

        public double getAverageScore() {
            if (scores.isEmpty()) return 0;
            int sum = 0;
            for (int i = 0; i < scores.size(); i++) {
                sum += scores.get(i).score;
            }
            return (double) sum / scores.size();
        }

        public int getMaxScore() {
            if (scores.isEmpty()) return 0;
            int max = scores.get(0).score;
            for (int i = 1; i < scores.size(); i++) {
                max = Math.max(max, scores.get(i).score);
            }
            return max;
        }

        public int getMinScore() {
            if (scores.isEmpty()) return 0;
            int min = scores.get(0).score;
            for (int i = 1; i < scores.size(); i++) {
                min = Math.min(min, scores.get(i).score);
            }
            return min;
        }

        public int getPassCount() {
            int count = 0;
            for (int i = 0; i < scores.size(); i++) {
                if (scores.get(i).score >= 60) {
                    count++;
                }
            }
            return count;
        }

        public int getStudentCount() {
            return scores.size();
        }

        static class StudentScore {
            String name;
            int score;

            StudentScore(String name, int score) {
                this.name = name;
                this.score = score;
            }
        }
    }

    /**
     * 购物车
     * 使用动态数组管理商品
     */
    static class ShoppingCart {
        private DynamicArrayImplementation<CartItem> items;

        public ShoppingCart() {
            items = new DynamicArrayImplementation<>();
        }

        public void addItem(String name, double price, int quantity) {
            items.add(new CartItem(name, price, quantity));
            System.out.println("✓ 已添加: " + name + " × " + quantity);
        }

        public void removeItem(int index) {
            if (index >= 0 && index < items.size()) {
                CartItem removed = items.remove(index);
                System.out.println("✗ 已删除: " + removed.name);
            }
        }

        public void updateQuantity(int index, int newQuantity) {
            if (index >= 0 && index < items.size()) {
                CartItem item = items.get(index);
                item.quantity = newQuantity;
                System.out.println("✓ 已更新: " + item.name + " 数量 -> " + newQuantity);
            }
        }

        public double getTotalPrice() {
            double total = 0;
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                total += item.price * item.quantity;
            }
            return total;
        }

        public void displayCart() {
            System.out.println("购物车清单:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                System.out.println((i + 1) + ". " + item.name +
                                 " - ¥" + item.price +
                                 " × " + item.quantity +
                                 " = ¥" + (item.price * item.quantity));
            }
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("商品数量: " + items.size());
        }

        static class CartItem {
            String name;
            double price;
            int quantity;

            CartItem(String name, double price, int quantity) {
                this.name = name;
                this.price = price;
                this.quantity = quantity;
            }
        }
    }
}
