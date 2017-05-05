package com.slavi.parser;

import java.util.Arrays;

public class Filter {
	protected int page = 1;
	protected int size = 10;
	protected String[] q;
	protected String[] sort;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String[] getQ() {
		return q;
	}
	public void setQ(String[] q) {
		this.q = q;
	}
	public String[] getSort() {
		return sort;
	}
	public void setSort(String[] sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "Filter [q=" + Arrays.toString(q) + ", sort=" + Arrays.toString(sort) + ", page=" + page + ", size=" + size + "]";
	}
}
