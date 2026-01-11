package com.architecture.algorithm.sort;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

public class SortingAlgorithmsTest {
    
    @Test
    public void testBubbleSort() {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        SortingAlgorithms.bubbleSort(arr);
        assertArrayEquals(expected, arr);
    }
    
    @Test
    public void testQuickSort() {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        SortingAlgorithms.quickSort(arr, 0, arr.length - 1);
        assertArrayEquals(expected, arr);
    }
    
    @Test
    public void testMergeSort() {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        SortingAlgorithms.mergeSort(arr, 0, arr.length - 1);
        assertArrayEquals(expected, arr);
    }
    
    @Test
    public void testHeapSort() {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        SortingAlgorithms.heapSort(arr);
        assertArrayEquals(expected, arr);
    }
    
    @Test
    public void testCountingSort() {
        int[] arr = {4, 2, 2, 8, 3, 3, 1};
        int[] expected = {1, 2, 2, 3, 3, 4, 8};
        SortingAlgorithms.countingSort(arr);
        assertArrayEquals(expected, arr);
    }
}