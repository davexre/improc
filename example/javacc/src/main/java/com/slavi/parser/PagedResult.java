package com.slavi.parser;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="result")
public class PagedResult<T> {

	int page;
	int size;
	boolean hasNext;
	List<T> items;

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

	public boolean isHasNext() {
		return hasNext;
	}

	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

}
