package com.slavi.util.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class IntIterator implements Iterator<Integer> {
	final AtomicInteger current;
	final int upto;

	public IntIterator(int count) {
		this(0, count);
	}

	public IntIterator(int start, int upto) {
		this.current = new AtomicInteger(start);
		this.upto = upto;
	}
	
	public boolean hasNext() {
		return current.get() < upto;
	}

	public Integer next() {
		int r = current.getAndIncrement();
		if (r < upto)
			return r;
		current.decrementAndGet();
		throw new NoSuchElementException();
	}
}
