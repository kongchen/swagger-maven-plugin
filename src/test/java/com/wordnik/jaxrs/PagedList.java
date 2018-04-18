package com.wordnik.jaxrs;

import java.util.List;

public class PagedList<T> {

	private final int pageNumber;
	private final int totalItems;
	private final List<T> items;

	public PagedList(final int pageNumber, final int totalItems, final List<T> itemsOnPage) {
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
