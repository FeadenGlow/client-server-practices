package practice2;

import java.util.LinkedList;
import java.util.Queue;

public class SharedQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private boolean closed = false;

    public SharedQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(T value) throws InterruptedException {
        while (queue.size() == capacity && !closed) {
            wait();
        }

        if (closed) {
            throw new IllegalStateException("Queue is closed");
        }

        queue.add(value);
        notifyAll();
    }

    public synchronized T consume() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait();
        }

        if (queue.isEmpty() && closed) {
            return null;
        }

        T value = queue.poll();
        notifyAll();
        return value;
    }

    public synchronized void close() {
        closed = true;
        notifyAll();
    }
}