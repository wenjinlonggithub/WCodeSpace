package com.architecture.principle;

/**
 * 简单动态字符串（SDS）- Redis String的底层实现
 *
 * SDS相比C字符串的优势：
 * 1. O(1)时间复杂度获取字符串长度
 * 2. 杜绝缓冲区溢出
 * 3. 减少修改字符串时内存重分配次数
 * 4. 二进制安全
 * 5. 兼容部分C字符串函数
 */
public class SimpleDynamicString {

    private char[] buf;      // 字节数组，用于保存字符串
    private int len;         // 已使用的字节数量
    private int free;        // 未使用的字节数量

    public SimpleDynamicString(String str) {
        if (str == null) {
            str = "";
        }
        this.len = str.length();
        // 空间预分配策略
        this.free = this.len;
        this.buf = new char[len + free];
        System.arraycopy(str.toCharArray(), 0, buf, 0, len);
    }

    /**
     * 获取字符串长度 - O(1)复杂度
     */
    public int length() {
        return len;
    }

    /**
     * 获取可用空间
     */
    public int avail() {
        return free;
    }

    /**
     * 追加字符串
     * 采用空间预分配策略避免频繁内存分配
     */
    public void append(String str) {
        if (str == null || str.isEmpty()) {
            return;
        }

        int appendLen = str.length();

        // 如果空闲空间不足，需要扩容
        if (free < appendLen) {
            expandCapacity(appendLen);
        }

        // 追加数据
        System.arraycopy(str.toCharArray(), 0, buf, len, appendLen);
        len += appendLen;
        free -= appendLen;
    }

    /**
     * 空间预分配策略
     * 1. 如果修改后len < 1MB，分配len大小的未使用空间
     * 2. 如果修改后len >= 1MB，分配1MB的未使用空间
     */
    private void expandCapacity(int addLen) {
        int newLen = len + addLen;
        int newFree;

        if (newLen < 1024 * 1024) {
            newFree = newLen;
        } else {
            newFree = 1024 * 1024;
        }

        char[] newBuf = new char[newLen + newFree];
        System.arraycopy(buf, 0, newBuf, 0, len);
        this.buf = newBuf;
        this.free = newFree + (free - (addLen > free ? 0 : addLen));
    }

    /**
     * 清空字符串 - 惰性空间释放
     * 不立即释放空间，而是记录到free中，供后续使用
     */
    public void clear() {
        free += len;
        len = 0;
    }

    /**
     * 获取字符串内容
     */
    @Override
    public String toString() {
        return new String(buf, 0, len);
    }

    /**
     * 二进制安全测试
     */
    public void setBinaryData(byte[] data) {
        clear();
        if (data == null || data.length == 0) {
            return;
        }

        if (free < data.length) {
            expandCapacity(data.length);
        }

        for (int i = 0; i < data.length; i++) {
            buf[i] = (char) (data[i] & 0xFF);
        }
        len = data.length;
        free -= data.length;
    }

    /**
     * 获取内存信息
     */
    public String getMemoryInfo() {
        return String.format("len=%d, free=%d, total=%d", len, free, buf.length);
    }
}
