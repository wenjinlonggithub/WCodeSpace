package com.architecture.algorithm.sort;

import java.util.Arrays;

/**
 * 排序算法实现类
 * 根据README中的描述，实现多种排序算法
 * 包括冒泡排序、快速排序、归并排序、堆排序等
 */
public class SortingAlgorithms {
    
    /**
     * 冒泡排序
     * 时间复杂度: 最好 O(n)，平均 O(n²)，最坏 O(n²)
     * 空间复杂度: O(1)
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    // 交换元素
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            // 如果没有发生交换，说明已经排序完成
            if (!swapped) break;
        }
    }
    
    /**
     * 选择排序
     * 时间复杂度: 最好 O(n²)，平均 O(n²)，最坏 O(n²)
     * 空间复杂度: O(1)
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            int temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }
    
    /**
     * 插入排序
     * 时间复杂度: 最好 O(n)，平均 O(n²)，最坏 O(n²)
     * 空间复杂度: O(1)
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; ++i) {
            int key = arr[i];
            int j = i - 1;
            
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j = j - 1;
            }
            arr[j + 1] = key;
        }
    }
    
    /**
     * 快速排序
     * 时间复杂度: 最好 O(n log n)，平均 O(n log n)，最坏 O(n²)
     * 空间复杂度: O(log n)
     */
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(arr, low, high);
            quickSort(arr, low, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, high);
        }
    }
    
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        
        return i + 1;
    }
    
    /**
     * 归并排序
     * 时间复杂度: 最好 O(n log n)，平均 O(n log n)，最坏 O(n log n)
     * 空间复杂度: O(n)
     */
    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    
    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];
        
        for (int i = 0; i < n1; ++i)
            leftArr[i] = arr[left + i];
        for (int j = 0; j < n2; ++j)
            rightArr[j] = arr[mid + 1 + j];
        
        int i = 0, j = 0, k = left;
        
        while (i < n1 && j < n2) {
            if (leftArr[i] <= rightArr[j]) {
                arr[k] = leftArr[i];
                i++;
            } else {
                arr[k] = rightArr[j];
                j++;
            }
            k++;
        }
        
        while (i < n1) {
            arr[k] = leftArr[i];
            i++;
            k++;
        }
        
        while (j < n2) {
            arr[k] = rightArr[j];
            j++;
            k++;
        }
    }
    
    /**
     * 堆排序
     * 时间复杂度: 最好 O(n log n)，平均 O(n log n)，最坏 O(n log n)
     * 空间复杂度: O(1)
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;
        
        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        
        // 逐个提取元素
        for (int i = n - 1; i > 0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            
            heapify(arr, i, 0);
        }
    }
    
    private static void heapify(int[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        
        if (left < n && arr[left] > arr[largest])
            largest = left;
        
        if (right < n && arr[right] > arr[largest])
            largest = right;
        
        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            
            heapify(arr, n, largest);
        }
    }
    
    /**
     * 计数排序
     * 时间复杂度: 最好 O(n+k)，平均 O(n+k)，最坏 O(n+k)
     * 空间复杂度: O(k)
     */
    public static void countingSort(int[] arr) {
        if (arr.length <= 1) return;
        
        int max = Arrays.stream(arr).max().orElse(0);
        int min = Arrays.stream(arr).min().orElse(0);
        int range = max - min + 1;
        int[] count = new int[range];
        int[] output = new int[arr.length];
        
        for (int i = 0; i < arr.length; i++) {
            count[arr[i] - min]++;
        }
        
        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }
        
        for (int i = arr.length - 1; i >= 0; i--) {
            output[count[arr[i] - min] - 1] = arr[i];
            count[arr[i] - min]--;
        }
        
        for (int i = 0; i < arr.length; i++) {
            arr[i] = output[i];
        }
    }
    
    /**
     * 排序算法性能测试
     */
    public static void performanceTest() {
        int[] sizes = {1000, 5000, 10000};
        
        for (int size : sizes) {
            System.out.println("\n测试数据规模: " + size + " 随机整数");
            
            // 生成随机数组
            int[] original = new int[size];
            for (int i = 0; i < size; i++) {
                original[i] = (int) (Math.random() * size);
            }
            
            // 测试快速排序
            int[] arr = original.clone();
            long startTime = System.nanoTime();
            quickSort(arr, 0, arr.length - 1);
            long endTime = System.nanoTime();
            System.out.println("快速排序: " + (endTime - startTime) / 1_000_000.0 + "ms");
            
            // 测试归并排序
            arr = original.clone();
            startTime = System.nanoTime();
            mergeSort(arr, 0, arr.length - 1);
            endTime = System.nanoTime();
            System.out.println("归并排序: " + (endTime - startTime) / 1_000_000.0 + "ms");
            
            // 测试堆排序
            arr = original.clone();
            startTime = System.nanoTime();
            heapSort(arr);
            endTime = System.nanoTime();
            System.out.println("堆排序: " + (endTime - startTime) / 1_000_000.0 + "ms");
            
            // 测试插入排序（仅对小数组）
            if (size <= 5000) {
                arr = original.clone();
                startTime = System.nanoTime();
                insertionSort(arr);
                endTime = System.nanoTime();
                System.out.println("插入排序: " + (endTime - startTime) / 1_000_000.0 + "ms");
            }
        }
    }
    
    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("原数组: " + Arrays.toString(arr));
        
        int[] bubbleArr = arr.clone();
        bubbleSort(bubbleArr);
        System.out.println("冒泡排序: " + Arrays.toString(bubbleArr));
        
        int[] quickArr = arr.clone();
        quickSort(quickArr, 0, quickArr.length - 1);
        System.out.println("快速排序: " + Arrays.toString(quickArr));
        
        int[] mergeArr = arr.clone();
        mergeSort(mergeArr, 0, mergeArr.length - 1);
        System.out.println("归并排序: " + Arrays.toString(mergeArr));
        
        int[] heapArr = arr.clone();
        heapSort(heapArr);
        System.out.println("堆排序: " + Arrays.toString(heapArr));
        
        // 性能测试
        System.out.println("\n性能测试:");
        performanceTest();
    }
}