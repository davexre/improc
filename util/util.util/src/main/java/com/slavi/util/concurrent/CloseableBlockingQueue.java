package com.slavi.util.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class CloseableBlockingQueue<T> implements AutoCloseable {

	final Object lock;

	Object items[];
	int capacity;
	int head;
	int tail;
	int size;
	boolean closed;

	public CloseableBlockingQueue() {
		this(10);
	}

	public CloseableBlockingQueue(int capacity) {
		if (capacity < 1)
			throw new IllegalArgumentException("Can not set queue capacity to " + capacity);
		items = new Object[capacity];
		this.capacity = capacity;
		closed = false;
		size = head = tail = 0;
		lock = new Object();
	}

	public boolean offer(T t) throws RejectedExecutionException {
		if (t == null)
			throw new NullPointerException();
		synchronized(lock) {
			if (closed)
				throw new RejectedExecutionException();
			if (capacity > resize()) {
				items[head] = t;
				head = (head + 1) % items.length;
				size++;
				lock.notify();
				return true;
			} else {
				return false;
			}
		}
	}

	public void put(T t) throws InterruptedException, RejectedExecutionException {
		if (t == null)
			throw new NullPointerException();
		synchronized(lock) {
			while(true) {
				if (closed)
					throw new RejectedExecutionException();
				if (capacity > resize()) {
					items[head] = t;
					head = (head + 1) % items.length;
					size++;
					lock.notify();
					break;
				} else {
					lock.wait();
				}
			}
		}
	}

	public T take() throws InterruptedException {
		synchronized(lock) {
			while(true) {
				if (size == 0) {
					if (closed)
						return null;
					lock.wait();
				} else {
					T result = (T) items[tail];
					tail = (tail + 1) % items.length;
					size--;
					lock.notify();
					return result;
				}
			}
		}
	}

	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		synchronized(lock) {
			if (size == 0) {
				if (closed)
					return null;
				lock.wait(unit.toMillis(timeout));
			}
			if (size == 0) {
				return null;
			} else {
				T result = (T) items[tail];
				tail = (tail + 1) % items.length;
				size--;
				lock.notify();
				return result;
			}
		}
	}


	public int getCapacity() {
		synchronized(lock) {
			return items.length;
		}
	}

	private int resize() {
		if (capacity != items.length && capacity >= size) {
			Object newItems[] = new Object[capacity];
			if (size == 0) {
				items = newItems;
				head = tail = 0;
			} else {
				if (tail <= head) {
					System.arraycopy(items, tail, newItems, 0, size);
				} else {
					System.arraycopy(items, tail, newItems, 0, items.length - tail);
					System.arraycopy(items, 0, newItems, items.length - tail, head);
				}
				items = newItems;
				tail = 0;
				head = size == capacity ? 0 : size;
			}
		}
		return size;
	}

	public int size() {
		synchronized(lock) {
			return size;
		}
	}

	public int remainingCapacity() {
		synchronized(lock) {
			int r = capacity - size;
			return r < 0 ? 0 : r;
		}
	}

	public int setCapacity(int capacity) {
		if (capacity < 1)
			throw new IllegalArgumentException("Can not set queue capacity to " + capacity);
		synchronized(lock) {
			this.capacity = capacity;
			return resize();
		}
	}

	public boolean isClosed() {
		synchronized(lock) {
			return closed;
		}
	}

	public boolean isEmpty() {
		synchronized(lock) {
			return size == 0;
		}
	}

	public void clear() {
		synchronized(lock) {
			head = tail = 0;
		}
	}

	public void close(boolean clearQueue) {
		synchronized(lock) {
			closed = true;
			if (clearQueue)
				head = tail = 0;
			lock.notifyAll();
		}
	}

	@Override
	public void close() {
		close(false);
	}
}
