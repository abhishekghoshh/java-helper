package com.behavioral.iterator;

public class Client {

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("Abhishek");
		list.add("Ghosh");
		System.out.println(list);

		System.out.println(list);

		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

}
