package com.architecture.jvm.jmm;

/**
 * 可见性问题演示
 *
 * 演示没有volatile时的可见性问题
 *
 * @author Architecture
 */
public class VisibilityProblemDemo {

    // 场景1: 没有volatile - 可能出现可见性问题
    private static boolean stopFlag = false;

    // 场景2: 使用volatile - 保证可见性
    private static volatile boolean volatileStopFlag = false;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 可见性问题演示 ===\n");

        // 演示1: 可见性问题
        demonstrateVisibilityProblem();

        Thread.sleep(1000);
        System.out.println("\n" + "=".repeat(50) + "\n");

        // 演示2: volatile解决可见性
        demonstrateVolatileSolution();
    }

    /**
     * 演示可见性问题
     * 注意: 由于JVM优化和现代CPU架构，这个问题可能不总是重现
     * 可以通过-Xint参数禁用JIT来增加重现概率
     */
    private static void demonstrateVisibilityProblem() throws InterruptedException {
        System.out.println("【演示1: 可见性问题】");
        System.out.println("说明: 线程可能看不到主线程对stopFlag的修改\n");

        Thread reader = new Thread(() -> {
            System.out.println("  [读线程] 启动，开始读取stopFlag...");
            int count = 0;
            while (!stopFlag) {
                count++;
                // 注意: 这里的空循环可能被JIT优化
                // 如果需要必现问题，可以添加: -Xint 参数
            }
            System.out.println("  [读线程] 检测到停止信号，退出! (循环了 " + count + " 次)");
        });

        reader.start();
        Thread.sleep(100); // 确保读线程已经启动

        System.out.println("  [主线程] 设置 stopFlag = true");
        stopFlag = true;

        // 等待最多2秒
        reader.join(2000);

        if (reader.isAlive()) {
            System.out.println("  [主线程] ❌ 可见性问题出现！读线程没有看到stopFlag的修改");
            System.out.println("  [主线程] 原因：读线程的CPU缓存中stopFlag仍是false");
            // 强制停止线程（仅用于演示）
            reader.interrupt();
        } else {
            System.out.println("  [主线程] ✓ 读线程正常退出（可能由于JVM优化）");
        }
    }

    /**
     * 演示volatile解决可见性问题
     */
    private static void demonstrateVolatileSolution() throws InterruptedException {
        System.out.println("【演示2: volatile解决可见性】");
        System.out.println("说明: 使用volatile保证多线程间的可见性\n");

        Thread reader = new Thread(() -> {
            System.out.println("  [读线程] 启动，读取volatile变量...");
            int count = 0;
            while (!volatileStopFlag) {
                count++;
                // volatile保证每次读取都从主内存获取最新值
            }
            System.out.println("  [读线程] ✓ 立即检测到停止信号，退出! (循环了 " + count + " 次)");
        });

        reader.start();
        Thread.sleep(100);

        System.out.println("  [主线程] 设置 volatileStopFlag = true (volatile写)");
        volatileStopFlag = true;

        reader.join(2000);

        if (reader.isAlive()) {
            System.out.println("  [主线程] ❌ 异常：即使使用volatile仍未退出");
        } else {
            System.out.println("  [主线程] ✓ volatile保证可见性，读线程立即看到修改并退出");
            System.out.println("\n原理:");
            System.out.println("  • volatile写：立即刷新到主内存");
            System.out.println("  • volatile读：从主内存读取最新值");
            System.out.println("  • 插入内存屏障，禁止指令重排序");
        }
    }
}
