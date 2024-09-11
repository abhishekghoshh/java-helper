package com.behavioral.iterator;

public class ListIterator<T> extends Iterator<T> {
	private List<T> list;
	private int index;

	public ListIterator(List<T> list) {
		this.index = 0;
		this.list = list;
	}

	@Override
	public boolean hasNext() {
		if (this.index < list.size()) {
			return true;
		}
		return false;
	}

	@Override
	public T next() {
		if (this.hasNext()) {
			return this.list.get(this.index++);
		}
		return null;
	}

}
