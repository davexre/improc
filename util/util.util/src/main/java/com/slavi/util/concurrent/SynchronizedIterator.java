package com.slavi.util.concurrent;

import java.util.Iterator;

public class SynchronizedIterator<T> implements Iterator<T> {
	final Iterator<T> iterator;

	public SynchronizedIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	public synchronized boolean hasNext() {
		return iterator.hasNext();
	}

	public synchronized T next() {
		return iterator.next();
	}
}
