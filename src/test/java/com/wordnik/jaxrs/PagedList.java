package com.wordnik.jaxrs;

import java.util.List;

public class PagedList<T> {

	private int pageNumber;
	private int totalItems;
	private List<T> items;

	public PagedList(int pageNumber, int totalItems, List<T> itemsOnPage) {
		this.pageNumber = pageNumber;
		this.totalItems = totalItems;
		this.items = itemsOnPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public List<T> getItems() {
		return items;
	}

}
