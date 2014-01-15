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
package com.alibaba.cobar.queue;

/**
 * @author xianmao.hexm
 */
public final class Queue<T> {

    private final static int MIN_SHRINK_SIZE = 1024;

    private T[] items;
    private int count = 0;
    private int start = 0, end = 0;
    private int suggestedSize, size = 0;

    public Queue(int suggestedSize) {
        this.size = this.suggestedSize = suggestedSize;
        items = newArray(this.size);
    }

    public Queue() {
        this(4);
    }

    public synchronized void clear() {
        count = start = end = 0;
        size = suggestedSize;
        items = newArray(size);
    }

    public synchronized boolean hasElements() {
        return (count != 0);
    }

    public synchronized int size() {
        return count;
    }

    public synchronized void prepend(T item) {
        if (count == size) {
            makeMoreRoom();
        }
        if (start == 0) {
            start = size - 1;
        } else {
            start--;
        }
        this.items[start] = item;
        count++;
        if (count == 1) {
            notify();
        }
    }

    public synchronized void append(T item) {
        append0(item, count == 0);
    }

    public synchronized void appendSilent(T item) {
        append0(item, false);
    }

    public synchronized void appendLoud(T item) {
        append0(item, true);
    }

    public synchronized T getNonBlocking() {
        if (count == 0) {
            return null;
        }
        // pull the object off, and clear our reference to it
        T retval = items[start];
        items[start] = null;
        start = (start + 1) % size;
        count--;
        return retval;
    }

    public synchronized void waitForItem() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized T get(long maxwait) {
        if (count == 0) {
            try {
                wait(maxwait);
            } catch (InterruptedException e) {
            }
            if (count == 0) {
                return null;
            }
        }
        return get();
    }

    public synchronized T get() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // pull the object off, and clear our reference to it
        T retval = items[start];
        items[start] = null;

        start = (start + 1) % size;
        count--;

        // if we are only filling 1/8th of the space, shrink by half
        if ((size > MIN_SHRINK_SIZE) && (size > suggestedSize) && (count < (size >> 3))) {
            shrink();
        }

        return retval;
    }

    private void append0(T item, boolean notify) {
        if (count == size) {
            makeMoreRoom();
        }
        this.items[end] = item;
        end = (end + 1) % size;
        count++;
        if (notify) {
            notify();
        }
    }

    private void makeMoreRoom() {
        T[] items = newArray(size * 2);
        System.arraycopy(this.items, start, items, 0, size - start);
        System.arraycopy(this.items, 0, items, size - start, end);
        start = 0;
        end = size;
        size *= 2;
        this.items = items;
    }

    // shrink by half
    private void shrink() {
        T[] items = newArray(size / 2);
        if (start > end) {
            // the data wraps around
            System.arraycopy(this.items, start, items, 0, size - start);
            System.arraycopy(this.items, 0, items, size - start, end + 1);
        } else {
            // the data does not wrap around
            System.arraycopy(this.items, start, items, 0, end - start + 1);
        }
        size = size / 2;
        start = 0;
        end = count;
        this.items = items;
    }

    @SuppressWarnings("unchecked")
    private T[] newArray(int size) {
        return (T[]) new Object[size];
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[count=").append(count);
        buf.append(", size=").append(size);
        buf.append(", start=").append(start);
        buf.append(", end=").append(end);
        buf.append(", elements={");
        for (int i = 0; i < count; i++) {
            int pos = (i + start) % size;
            if (i > 0)
                buf.append(", ");
            buf.append(items[pos]);
        }
        buf.append("}]");
        return buf.toString();
    }

}
