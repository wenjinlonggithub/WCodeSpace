package com.architecture.algorithm.search;

/**
 * 查找算法实现类
 * 根据README中的描述，实现线性查找、二分查找、哈希查找等算法
 */
public class SearchAlgorithms {
    
    /**
     * 线性查找
     * 时间复杂度: O(n)
     * 空间复杂度: O(1)
     */
    public static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i; // 返回找到元素的索引
            }
        }
        return -1; // 未找到返回-1
    }
    
    /**
     * 二分查找（数组必须已排序）
     * 时间复杂度: O(log n)
     * 空间复杂度: O(1)
     */
    public static int binarySearch(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            if (arr[mid] == target) {
                return mid;
            }
            
            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1; // 未找到返回-1
    }
    
    /**
     * 递归实现的二分查找
     * 时间复杂度: O(log n)
     * 空间复杂度: O(log n) - 由于递归调用栈
     */
    public static int binarySearchRecursive(int[] arr, int target, int left, int right) {
        if (left > right) {
            return -1; // 未找到
        }
        
        int mid = left + (right - left) / 2;
        
        if (arr[mid] == target) {
            return mid;
        }
        
        if (arr[mid] < target) {
            return binarySearchRecursive(arr, target, mid + 1, right);
        } else {
            return binarySearchRecursive(arr, target, left, mid - 1);
        }
    }
    
    /**
     * 插值查找（适用于均匀分布的数据）
     * 时间复杂度: O(log log n) 平均情况，O(n) 最坏情况
     * 空间复杂度: O(1)
     */
    public static int interpolationSearch(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        
        while (left <= right && target >= arr[left] && target <= arr[right]) {
            if (left == right) {
                if (arr[left] == target) {
                    return left;
                }
                return -1;
            }
            
            // 插值公式计算中间位置
            int pos = left + ((target - arr[left]) * (right - left)) / (arr[right] - arr[left]);
            
            if (arr[pos] == target) {
                return pos;
            }
            
            if (arr[pos] < target) {
                left = pos + 1;
            } else {
                right = pos - 1;
            }
        }
        
        return -1; // 未找到
    }
    
    /**
     * 指数查找（结合二分查找）
     * 时间复杂度: O(log n)
     * 空间复杂度: O(1)
     */
    public static int exponentialSearch(int[] arr, int target) {
        if (arr[0] == target) {
            return 0;
        }
        
        int i = 1;
        while (i < arr.length && arr[i] <= target) {
            i *= 2;
        }
        
        return binarySearchRecursive(arr, target, i / 2, Math.min(i, arr.length - 1));
    }
    
    /**
     * 跳跃查找
     * 时间复杂度: O(√n)
     * 空间复杂度: O(1)
     */
    public static int jumpSearch(int[] arr, int target) {
        int n = arr.length;
        int step = (int) Math.sqrt(n);
        int prev = 0;
        
        while (arr[Math.min(step, n) - 1] < target) {
            prev = step;
            step += (int) Math.sqrt(n);
            if (prev >= n) {
                return -1;
            }
        }
        
        while (arr[prev] < target) {
            prev++;
            
            if (prev == Math.min(step, n)) {
                return -1;
            }
        }
        
        if (arr[prev] == target) {
            return prev;
        }
        
        return -1;
    }
    
    /**
     * 查找算法性能测试
     */
    public static void performanceTest() {
        // 创建大型已排序数组用于测试
        int size = 1000000;
        int[] sortedArr = new int[size];
        for (int i = 0; i < size; i++) {
            sortedArr[i] = i * 2; // 偶数数组
        }
        
        int target = size; // 目标值
        
        System.out.println("测试数据规模: " + size + " 有序整数");
        System.out.println("目标值: " + target);
        
        // 测试二分查找
        long startTime = System.nanoTime();
        int result = binarySearch(sortedArr, target);
        long endTime = System.nanoTime();
        System.out.println("二分查找: " + (endTime - startTime) / 1_000_000.0 + "ms, 结果: " + result);
        
        // 测试插值查找
        startTime = System.nanoTime();
        result = interpolationSearch(sortedArr, target);
        endTime = System.nanoTime();
        System.out.println("插值查找: " + (endTime - startTime) / 1_000_000.0 + "ms, 结果: " + result);
        
        // 测试指数查找
        startTime = System.nanoTime();
        result = exponentialSearch(sortedArr, target);
        endTime = System.nanoTime();
        System.out.println("指数查找: " + (endTime - startTime) / 1_000_000.0 + "ms, 结果: " + result);
        
        // 测试跳跃查找
        startTime = System.nanoTime();
        result = jumpSearch(sortedArr, target);
        endTime = System.nanoTime();
        System.out.println("跳跃查找: " + (endTime - startTime) / 1_000_000.0 + "ms, 结果: " + result);
        
        // 注意：对于大规模数据，线性查找会非常慢，这里只用小数组演示
        int[] smallArr = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19};
        target = 11;
        startTime = System.nanoTime();
        result = linearSearch(smallArr, target);
        endTime = System.nanoTime();
        System.out.println("线性查找(小数组): " + (endTime - startTime) / 1_000_000.0 + "ms, 结果: " + result);
    }
    
    public static void main(String[] args) {
        int[] arr = {2, 3, 4, 10, 40, 50, 80, 90, 100};
        int target = 10;
        
        System.out.println("数组: ");
        for (int val : arr) {
            System.out.print(val + " ");
        }
        System.out.println("\n目标值: " + target);
        
        int result = linearSearch(arr, target);
        System.out.println("线性查找结果: " + result);
        
        result = binarySearch(arr, target);
        System.out.println("二分查找结果: " + result);
        
        result = binarySearchRecursive(arr, target, 0, arr.length - 1);
        System.out.println("递归二分查找结果: " + result);
        
        result = interpolationSearch(arr, target);
        System.out.println("插值查找结果: " + result);
        
        result = exponentialSearch(arr, target);
        System.out.println("指数查找结果: " + result);
        
        result = jumpSearch(arr, target);
        System.out.println("跳跃查找结果: " + result);
        
        System.out.println("\n性能测试:");
        performanceTest();
    }
}