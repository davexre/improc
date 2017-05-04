package com.slavi.parser;

public class Paging {
	int page = 1;
	int size = 10;
	String sort = "";

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

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "Paging [page=" + page + ", size=" + size + ", sort=" + sort + "]";
	}
}
