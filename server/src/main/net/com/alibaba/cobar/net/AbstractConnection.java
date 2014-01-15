/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cobar.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.cobar.config.ErrorCode;
import com.alibaba.cobar.net.buffer.BufferPool;
import com.alibaba.cobar.net.buffer.BufferQueue;
import com.alibaba.cobar.util.TimeUtil;

/**
 * @author xianmao.hexm
 */
public abstract class AbstractConnection implements NIOConnection {
    private static final int OP_NOT_READ = ~SelectionKey.OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;

    protected final SocketChannel channel;
    protected NIOProcessor processor;
    protected SelectionKey processKey;
    protected final ReentrantLock keyLock;
    protected int packetHeaderSize;
    protected int maxPacketSize;
    protected int readBufferOffset;
    protected ByteBuffer readBuffer;
    protected BufferQueue writeQueue;
    protected final ReentrantLock writeLock;
    protected boolean isRegistered;
    protected final AtomicBoolean isClosed;
    protected boolean isSocketClosed;
    protected long startupTime;
    protected long lastReadTime;
    protected long lastWriteTime;
    protected long netInBytes;
    protected long netOutBytes;
    protected int writeAttempts;

    public AbstractConnection(SocketChannel channel) {
        this.channel = channel;
        this.keyLock = new ReentrantLock();
        this.writeLock = new ReentrantLock();
        this.isClosed = new AtomicBoolean(false);
        this.startupTime = TimeUtil.currentTimeMillis();
        this.lastReadTime = startupTime;
        this.lastWriteTime = startupTime;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public int getPacketHeaderSize() {
        return packetHeaderSize;
    }

    public void setPacketHeaderSize(int packetHeaderSize) {
        this.packetHeaderSize = packetHeaderSize;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public int getWriteAttempts() {
        return writeAttempts;
    }

    public NIOProcessor getProcessor() {
        return processor;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public BufferQueue getWriteQueue() {
        return writeQueue;
    }

    public void setWriteQueue(BufferQueue writeQueue) {
        this.writeQueue = writeQueue;
    }

    /**
     * 分配缓存
     */
    public ByteBuffer allocate() {
        return processor.getBufferPool().allocate();
    }

    /**
     * 回收缓存
     */
    public void recycle(ByteBuffer buffer) {
        processor.getBufferPool().recycle(buffer);
    }

    @Override
    public void register(Selector selector) throws IOException {
        try {
            processKey = channel.register(selector, SelectionKey.OP_READ, this);
            isRegistered = true;
        } finally {
            if (isClosed.get()) {
                clearSelectionKey();
            }
        }
    }

    @Override
    public void read() throws IOException {
        ByteBuffer buffer = this.readBuffer;
        int got = channel.read(buffer);
        lastReadTime = TimeUtil.currentTimeMillis();
        if (got < 0) {
            throw new EOFException();
        }
        netInBytes += got;
        processor.addNetInBytes(got);

        // 处理数据
        int offset = readBufferOffset, length = 0, position = buffer.position();
        for (;;) {
            length = getPacketLength(buffer, offset);
            if (length == -1) {// 未达到可计算数据包长度的数据
                if (!buffer.hasRemaining()) {
                    checkReadBuffer(buffer, offset, position);
                }
                break;
            }
            if (position >= offset + length) {
                // 提取一个数据包的数据进行处理
                buffer.position(offset);
                byte[] data = new byte[length];
                buffer.get(data, 0, length);
                handle(data);

                // 设置偏移量
                offset += length;
                if (position == offset) {// 数据正好全部处理完毕
                    if (readBufferOffset != 0) {
                        readBufferOffset = 0;
                    }
                    buffer.clear();
                    break;
                } else {// 还有剩余数据未处理
                    readBufferOffset = offset;
                    buffer.position(position);
                    continue;
                }
            } else {// 未到达一个数据包的数据
                if (!buffer.hasRemaining()) {
                    checkReadBuffer(buffer, offset, position);
                }
                break;
            }
        }
    }

    public void write(byte[] data) {
        ByteBuffer buffer = allocate();
        buffer = writeToBuffer(data, buffer);
        write(buffer);
    }

    @Override
    public void write(ByteBuffer buffer) {
        if (isClosed.get()) {
            processor.getBufferPool().recycle(buffer);
            return;
        }
        if (isRegistered) {
            try {
                writeQueue.put(buffer);
            } catch (InterruptedException e) {
                error(ErrorCode.ERR_PUT_WRITE_QUEUE, e);
                return;
            }
            processor.postWrite(this);
        } else {
            processor.getBufferPool().recycle(buffer);
            close();
        }
    }

    @Override
    public void writeByQueue() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // 满足以下两个条件时，切换到基于事件的写操作。
            // 1.当前key对写事件不该兴趣。
            // 2.write0()返回false。
            if ((processKey.interestOps() & SelectionKey.OP_WRITE) == 0 && !write0()) {
                enableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeByEvent() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // 满足以下两个条件时，切换到基于队列的写操作。
            // 1.write0()返回true。
            // 2.发送队列的buffer为空。
            if (write0() && writeQueue.size() == 0) {
                disableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 打开读事件
     */
    public void enableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * 关闭读事件
     */
    public void disableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_READ);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查WriteBuffer容量，不够则写出当前缓存块并申请新的缓存块。
     */
    public ByteBuffer checkWriteBuffer(ByteBuffer buffer, int capacity) {
        if (capacity > buffer.remaining()) {
            write(buffer);
            return processor.getBufferPool().allocate();
        } else {
            return buffer;
        }
    }

    /**
     * 把数据写到给定的缓存中，如果满了则提交当前缓存并申请新的缓存。
     */
    public ByteBuffer writeToBuffer(byte[] src, ByteBuffer buffer) {
        int offset = 0;
        int length = src.length;
        int remaining = buffer.remaining();
        while (length > 0) {
            if (remaining >= length) {
                buffer.put(src, offset, length);
                break;
            } else {
                buffer.put(src, offset, remaining);
                write(buffer);
                buffer = processor.getBufferPool().allocate();
                offset += remaining;
                length -= remaining;
                remaining = buffer.remaining();
                continue;
            }
        }
        return buffer;
    }

    @Override
    public boolean close() {
        if (isClosed.get()) {
            return false;
        } else {
            if (closeSocket()) {
                return isClosed.compareAndSet(false, true);
            } else {
                return false;
            }
        }
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    /**
     * 由Processor调用的空闲检查
     */
    protected abstract void idleCheck();

    /**
     * 清理遗留资源
     */
    protected void cleanup() {
        BufferPool pool = processor.getBufferPool();
        ByteBuffer buffer = null;

        // 回收接收缓存
        buffer = this.readBuffer;
        if (buffer != null) {
            this.readBuffer = null;
            pool.recycle(buffer);
        }

        // 回收发送缓存
        while ((buffer = writeQueue.poll()) != null) {
            pool.recycle(buffer);
        }
    }

    /**
     * 获取数据包长度，默认是MySQL数据包，其他数据包重载此方法。
     */
    protected int getPacketLength(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + packetHeaderSize) {
            return -1;
        } else {
            int length = buffer.get(offset) & 0xff;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 16;
            return length + packetHeaderSize;
        }
    }

    /**
     * 检查ReadBuffer容量，不够则扩展当前缓存，直到最大值。
     */
    private ByteBuffer checkReadBuffer(ByteBuffer buffer, int offset, int position) {
        // 当偏移量为0时需要扩容，否则移动数据至偏移量为0的位置。
        if (offset == 0) {
            if (buffer.capacity() >= maxPacketSize) {
                throw new IllegalArgumentException("Packet size over the limit.");
            }
            int size = buffer.capacity() << 1;
            size = (size > maxPacketSize) ? maxPacketSize : size;
            ByteBuffer newBuffer = ByteBuffer.allocate(size);
            buffer.position(offset);
            newBuffer.put(buffer);
            readBuffer = newBuffer;
            // 回收扩容前的缓存块
            processor.getBufferPool().recycle(buffer);
            return newBuffer;
        } else {
            buffer.position(offset);
            buffer.compact();
            readBufferOffset = 0;
            return buffer;
        }
    }

    private boolean write0() throws IOException {
        // 检查是否有遗留数据未写出
        ByteBuffer buffer = writeQueue.attachment();
        if (buffer != null) {
            int written = channel.write(buffer);
            if (written > 0) {
                netOutBytes += written;
                processor.addNetOutBytes(written);
            }
            lastWriteTime = TimeUtil.currentTimeMillis();
            if (buffer.hasRemaining()) {
                writeAttempts++;
                return false;
            } else {
                writeQueue.attach(null);
                processor.getBufferPool().recycle(buffer);
            }
        }
        // 写出发送队列中的数据块
        if ((buffer = writeQueue.poll()) != null) {
            // 如果是一块未使用过的buffer，则执行关闭连接。
            if (buffer.position() == 0) {
                processor.getBufferPool().recycle(buffer);
                close();
                return true;
            }
            buffer.flip();
            int written = channel.write(buffer);
            if (written > 0) {
                netOutBytes += written;
                processor.addNetOutBytes(written);
            }
            lastWriteTime = TimeUtil.currentTimeMillis();
            if (buffer.hasRemaining()) {
                writeQueue.attach(buffer);
                writeAttempts++;
                return false;
            } else {
                processor.getBufferPool().recycle(buffer);
            }
        }
        return true;
    }

    /**
     * 打开写事件
     */
    private void enableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * 关闭写事件
     */
    private void disableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } finally {
            lock.unlock();
        }
    }

    private void clearSelectionKey() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            if (key != null && key.isValid()) {
                key.attach(null);
                key.cancel();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean closeSocket() {
        clearSelectionKey();
        SocketChannel channel = this.channel;
        if (channel != null) {
            boolean isSocketClosed = true;
            Socket socket = channel.socket();
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e) {
                }
                isSocketClosed = socket.isClosed();
            }
            try {
                channel.close();
            } catch (Throwable e) {
            }
            return isSocketClosed && (!channel.isOpen());
        } else {
            return true;
        }
    }

}
