package com.behavioral.iterator;

import java.util.Arrays;

public class ArrayList<T> implements List<T> {

	private int capacity = 16;
	private T[] list;
	private int index = 0;

	@SuppressWarnings("unchecked")
	public ArrayList() {
		list = (T[]) new Object[capacity];
	}

	@Override
	public Iterator<T> iterator() {
		return new ListIterator<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(T object) {
		if (this.index < this.capacity) {
			list[index++] = object;
		} else {
			T[] newList = (T[]) new Object[2 * capacity];
			for (int i = 0; i < capacity; i++)
				newList[i] = list[i];
			capacity = capacity * 2;
			list = newList;
			add(object);
		}
	}

	@Override
	public T get(int index) {
		return list[index];
	}

	@Override
	public void add(List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			add(list.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		list = (T[]) new Object[capacity];
	}

	@Override
	public int size() {
		return index;
	}

	@Override
	public String toString() {
		return "ArrayList " + Arrays.toString(list);
	}

}
