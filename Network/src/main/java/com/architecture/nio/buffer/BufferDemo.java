package com.architecture.nio.buffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

/**
 * Java NIO Buffer 核心组件示例
 *
 * Buffer是NIO的核心组件之一，用于与Channel进行交互
 * 本质上是一个可以读写数据的内存块，被包装成了Buffer对象
 *
 * Buffer的四个核心属性：
 * 1. capacity（容量）：Buffer能够容纳的数据元素的最大数量，创建时设定且不可改变
 * 2. limit（限制）：Buffer中不可读写的第一个元素的索引，即可以读写的元素数量
 * 3. position（位置）：下一个要读取或写入的元素的索引
 * 4. mark（标记）：一个备忘位置，用于记录上一次读写的位置
 *
 * 关系：0 <= mark <= position <= limit <= capacity
 */
public class BufferDemo {

    /**
     * 演示ByteBuffer的基本操作
     */
    public static void demonstrateByteBuffer() {
        System.out.println("=== ByteBuffer 基本操作 ===");

        // 1. 创建Buffer - 分配10个字节的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        printBufferStatus("初始化后", buffer);

        // 2. 写入数据
        buffer.put((byte) 'H');
        buffer.put((byte) 'e');
        buffer.put((byte) 'l');
        buffer.put((byte) 'l');
        buffer.put((byte) 'o');
        printBufferStatus("写入'Hello'后", buffer);

        // 3. flip() - 切换到读模式
        // flip会将limit设置为当前position，position重置为0
        buffer.flip();
        printBufferStatus("flip()后", buffer);

        // 4. 读取数据
        System.out.println("读取的数据：");
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        System.out.println();
        printBufferStatus("读取完成后", buffer);

        // 5. rewind() - 重新读取
        // 将position设置为0，可以重新读取buffer中的数据
        buffer.rewind();
        printBufferStatus("rewind()后", buffer);

        // 6. clear() - 清空缓冲区
        // 将position设置为0，limit设置为capacity，准备重新写入
        buffer.clear();
        printBufferStatus("clear()后", buffer);

        // 7. compact() 示例
        buffer.put((byte) 'A');
        buffer.put((byte) 'B');
        buffer.put((byte) 'C');
        buffer.flip();
        buffer.get(); // 读取一个字节
        printBufferStatus("读取一个字节后", buffer);

        // compact会将未读的数据复制到Buffer起始处
        buffer.compact();
        printBufferStatus("compact()后", buffer);
    }

    /**
     * 演示mark和reset操作
     */
    public static void demonstrateMarkAndReset() {
        System.out.println("\n=== Mark 和 Reset 操作 ===");

        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) '1');
        buffer.put((byte) '2');
        buffer.put((byte) '3');
        buffer.put((byte) '4');
        buffer.put((byte) '5');

        buffer.flip();

        // 读取两个字节
        System.out.println("读取: " + (char) buffer.get());
        System.out.println("读取: " + (char) buffer.get());

        // 标记当前位置
        buffer.mark();
        printBufferStatus("mark()后", buffer);

        // 继续读取
        System.out.println("读取: " + (char) buffer.get());
        System.out.println("读取: " + (char) buffer.get());
        printBufferStatus("继续读取后", buffer);

        // 重置到标记位置
        buffer.reset();
        printBufferStatus("reset()后", buffer);
        System.out.println("重置后读取: " + (char) buffer.get());
    }

    /**
     * 演示直接缓冲区和非直接缓冲区
     */
    public static void demonstrateDirectBuffer() {
        System.out.println("\n=== 直接缓冲区 vs 非直接缓冲区 ===");

        // 非直接缓冲区：在JVM堆内存中分配
        ByteBuffer heapBuffer = ByteBuffer.allocate(1024);
        System.out.println("堆缓冲区isDirect: " + heapBuffer.isDirect());

        // 直接缓冲区：在操作系统内存中分配
        // 优点：减少一次数据拷贝，提高I/O性能
        // 缺点：分配和释放开销较大
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println("直接缓冲区isDirect: " + directBuffer.isDirect());

        System.out.println("\n直接缓冲区特点：");
        System.out.println("1. 分配在操作系统的本地内存中，不在JVM堆中");
        System.out.println("2. 减少了数据在JVM和操作系统间的拷贝");
        System.out.println("3. 适合大文件I/O操作");
        System.out.println("4. 创建和销毁成本较高");
    }

    /**
     * 演示不同类型的Buffer
     */
    public static void demonstrateTypedBuffers() {
        System.out.println("\n=== 不同类型的Buffer ===");

        // IntBuffer
        IntBuffer intBuffer = IntBuffer.allocate(5);
        intBuffer.put(10);
        intBuffer.put(20);
        intBuffer.put(30);
        intBuffer.flip();

        System.out.println("IntBuffer内容：");
        while (intBuffer.hasRemaining()) {
            System.out.print(intBuffer.get() + " ");
        }
        System.out.println();

        // CharBuffer
        CharBuffer charBuffer = CharBuffer.allocate(10);
        charBuffer.put("NIO");
        charBuffer.flip();

        System.out.println("CharBuffer内容：");
        while (charBuffer.hasRemaining()) {
            System.out.print(charBuffer.get());
        }
        System.out.println();

        // 只读Buffer
        ByteBuffer readOnlyBuffer = ByteBuffer.allocate(10);
        readOnlyBuffer.put((byte) 'A');
        ByteBuffer readOnly = readOnlyBuffer.asReadOnlyBuffer();
        System.out.println("只读Buffer: " + readOnly.isReadOnly());
    }

    /**
     * 打印Buffer的状态信息
     */
    private static void printBufferStatus(String operation, ByteBuffer buffer) {
        System.out.println(operation + ":");
        System.out.println("  position=" + buffer.position() +
                         ", limit=" + buffer.limit() +
                         ", capacity=" + buffer.capacity() +
                         ", remaining=" + buffer.remaining());
    }

    public static void main(String[] args) {
        demonstrateByteBuffer();
        demonstrateMarkAndReset();
        demonstrateDirectBuffer();
        demonstrateTypedBuffers();
    }
}
