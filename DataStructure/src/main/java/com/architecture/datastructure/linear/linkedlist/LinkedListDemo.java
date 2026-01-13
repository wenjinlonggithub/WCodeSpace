package com.architecture.datastructure.linear.linkedlist;

/**
 * LinkedList 实际应用场景演示
 * LinkedList Practical Use Case Demonstrations
 *
 * <p>应用场景:
 * <ol>
 *   <li>LRU缓存实现 - 使用双向链表+哈希表</li>
 *   <li>浏览器历史记录 - 前进/后退功能</li>
 *   <li>音乐播放器播放列表 - 循环播放</li>
 *   <li>文本编辑器的撤销/重做 - 操作历史</li>
 *   <li>任务调度器 - 动态任务队列</li>
 * </ol>
 *
 * @author Architecture Team
 * @version 1.0
 * @since 2026-01-13
 */
public class LinkedListDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   LinkedList 实际应用演示                    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        // 场景1: 任务队列管理
        demonstrateTaskQueue();

        // 场景2: 多项式计算
        demonstratePolynomial();

        // 场景3: 播放列表管理
        demonstrateMusicPlaylist();
    }

    /**
     * 场景1: 任务队列管理
     * 使用链表实现动态任务调度
     *
     * 实际应用: 操作系统进程调度、消息队列、异步任务处理
     */
    private static void demonstrateTaskQueue() {
        System.out.println("【场景1: 任务队列管理】");
        System.out.println("应用: 操作系统进程调度、消息队列、异步任务处理\n");

        LinkedListImplementation<String> taskQueue = new LinkedListImplementation<>();

        // 添加普通任务到队尾
        System.out.println(">>> 添加普通任务:");
        taskQueue.addLast("任务1: 发送邮件");
        taskQueue.addLast("任务2: 生成报告");
        taskQueue.addLast("任务3: 备份数据");
        System.out.println("当前任务队列: " + taskQueue);
        System.out.println("队列大小: " + taskQueue.size());
        System.out.println();

        // 紧急任务插入队首
        System.out.println(">>> 插入紧急任务:");
        taskQueue.addFirst("紧急任务: 系统告警处理");
        System.out.println("插入紧急任务后: " + taskQueue);
        System.out.println();

        // 执行任务(从队首移除)
        System.out.println(">>> 执行任务:");
        String currentTask = taskQueue.removeFirst();
        System.out.println("正在执行: " + currentTask);
        System.out.println("剩余任务: " + taskQueue);
        System.out.println("剩余任务数: " + taskQueue.size());
        System.out.println();

        // 取消特定任务
        System.out.println(">>> 取消任务:");
        boolean removed = taskQueue.removeElement("任务2: 生成报告");
        System.out.println("取消结果: " + (removed ? "成功" : "失败"));
        System.out.println("最终任务列表: " + taskQueue);

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 场景2: 多项式表示和计算
     * 使用链表存储多项式的系数和指数
     *
     * 实际应用: 符号计算、代数系统、计算机代数
     */
    private static void demonstratePolynomial() {
        System.out.println("【场景2: 多项式表示和计算】");
        System.out.println("应用: 符号计算、代数系统、计算机代数\n");

        // 多项式: 3x^2 + 5x + 7
        System.out.println(">>> 表示多项式: 3x² + 5x + 7");

        LinkedListImplementation<Term> polynomial = new LinkedListImplementation<>();
        polynomial.addLast(new Term(3, 2));  // 3x^2
        polynomial.addLast(new Term(5, 1));  // 5x
        polynomial.addLast(new Term(7, 0));  // 7

        System.out.println("链表表示: " + polynomial);
        System.out.println();

        // 计算多项式的值 (x = 2)
        int x = 2;
        int result = 0;
        for (int i = 0; i < polynomial.size(); i++) {
            Term term = polynomial.get(i);
            result += term.coefficient * Math.pow(x, term.exponent);
        }

        System.out.println(">>> 计算 x = " + x + " 时的值:");
        System.out.println("3×2² + 5×2 + 7 = " + result);
        System.out.println();

        // 添加新项
        System.out.println(">>> 添加新项: 2x³");
        polynomial.addFirst(new Term(2, 3));  // 添加到最前面
        System.out.println("新多项式: " + polynomial);
        System.out.println("表达式: 2x³ + 3x² + 5x + 7");

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 场景3: 音乐播放列表管理
     * 使用链表管理播放队列，支持插入、删除、跳转
     *
     * 实际应用: 媒体播放器、音乐应用、视频应用
     */
    private static void demonstrateMusicPlaylist() {
        System.out.println("【场景3: 音乐播放列表】");
        System.out.println("应用: 媒体播放器、音乐应用、视频应用\n");

        MusicPlaylist playlist = new MusicPlaylist();

        // 添加歌曲
        System.out.println(">>> 创建播放列表:");
        playlist.addSong("周杰伦 - 稻香");
        playlist.addSong("林俊杰 - 江南");
        playlist.addSong("邓紫棋 - 光年之外");
        playlist.addSong("薛之谦 - 演员");
        playlist.displayPlaylist();
        System.out.println();

        // 播放当前歌曲
        System.out.println(">>> 播放操作:");
        playlist.playCurrent();
        System.out.println();

        // 下一首
        playlist.next();
        playlist.playCurrent();
        System.out.println();

        // 上一首
        playlist.previous();
        playlist.playCurrent();
        System.out.println();

        // 移除当前歌曲
        System.out.println(">>> 移除当前歌曲:");
        playlist.removeCurrent();
        playlist.displayPlaylist();
        System.out.println();

        // 插入新歌到当前位置后
        System.out.println(">>> 插入新歌:");
        playlist.insertAfterCurrent("陈奕迅 - 十年");
        playlist.displayPlaylist();

        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }

    /**
     * 多项式项 - 系数和指数
     * Polynomial Term - Coefficient and Exponent
     */
    static class Term {
        int coefficient;  // 系数
        int exponent;     // 指数

        public Term(int coefficient, int exponent) {
            this.coefficient = coefficient;
            this.exponent = exponent;
        }

        @Override
        public String toString() {
            if (exponent == 0) {
                return String.valueOf(coefficient);
            }
            if (exponent == 1) {
                return coefficient + "x";
            }
            return coefficient + "x^" + exponent;
        }
    }

    /**
     * 音乐播放列表类
     * Music Playlist Class
     */
    static class MusicPlaylist {
        private LinkedListImplementation<String> songs;
        private int currentIndex;

        public MusicPlaylist() {
            this.songs = new LinkedListImplementation<>();
            this.currentIndex = 0;
        }

        /**
         * 添加歌曲到播放列表
         */
        public void addSong(String song) {
            songs.addLast(song);
        }

        /**
         * 播放当前歌曲
         */
        public void playCurrent() {
            if (songs.isEmpty()) {
                System.out.println("播放列表为空");
                return;
            }
            System.out.println("♪ 正在播放: " + songs.get(currentIndex));
        }

        /**
         * 下一首
         */
        public void next() {
            if (songs.isEmpty()) {
                return;
            }
            currentIndex = (currentIndex + 1) % songs.size();  // 循环播放
            System.out.println("→ 切换到下一首");
        }

        /**
         * 上一首
         */
        public void previous() {
            if (songs.isEmpty()) {
                return;
            }
            currentIndex = (currentIndex - 1 + songs.size()) % songs.size();  // 循环播放
            System.out.println("← 切换到上一首");
        }

        /**
         * 移除当前歌曲
         */
        public void removeCurrent() {
            if (songs.isEmpty()) {
                return;
            }
            String removed = songs.remove(currentIndex);
            System.out.println("✗ 已移除: " + removed);
            if (currentIndex >= songs.size()) {
                currentIndex = 0;
            }
        }

        /**
         * 在当前位置后插入歌曲
         */
        public void insertAfterCurrent(String song) {
            if (songs.isEmpty()) {
                songs.addFirst(song);
            } else {
                songs.add(currentIndex + 1, song);
            }
            System.out.println("✓ 已插入: " + song);
        }

        /**
         * 显示播放列表
         */
        public void displayPlaylist() {
            System.out.println("播放列表:");
            for (int i = 0; i < songs.size(); i++) {
                String marker = (i == currentIndex) ? "► " : "  ";
                System.out.println(marker + (i + 1) + ". " + songs.get(i));
            }
        }
    }
}
