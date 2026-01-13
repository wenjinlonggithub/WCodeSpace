package com.architecture.datastructure.advanced.bitset;

/**
 * 位集合 - BitSet
 * 用于高效存储布尔值
 */
public class BitSetImplementation {
    private long[] words;
    private int size;

    public BitSetImplementation(int size) {
        this.size = size;
        this.words = new long[(size + 63) / 64];  // 每个long存储64位
    }

    public void set(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) throw new IndexOutOfBoundsException();
        int wordIndex = bitIndex / 64;
        int bitPosition = bitIndex % 64;
        words[wordIndex] |= (1L << bitPosition);
    }

    public void clear(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) throw new IndexOutOfBoundsException();
        int wordIndex = bitIndex / 64;
        int bitPosition = bitIndex % 64;
        words[wordIndex] &= ~(1L << bitPosition);
    }

    public boolean get(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) throw new IndexOutOfBoundsException();
        int wordIndex = bitIndex / 64;
        int bitPosition = bitIndex % 64;
        return (words[wordIndex] & (1L << bitPosition)) != 0;
    }

    public int cardinality() {
        int count = 0;
        for (long word : words) {
            count += Long.bitCount(word);
        }
        return count;
    }
}
