package com.behavioral.iterator;

public interface Collection<T> {
	public Iterator<T> iterator();
}

interface List<T> extends Collection<T> {
	void add(T object);

	T get(int index);

	void add(List<T> list);

	void clear();

	int size();
}
