package com.creational.builder;

public class Client {

	public static void main(String[] args) {
		// Creating object using CakeBuilder pattern in java
		Cake cake = new Cake.CakeBuilder()
				.sugar(1)
				.butter(0.5)
				.eggs(2)
				.vanila(2)
				.flour(1.5)
				.bakingpowder(0.75)
				.milk(0.5)
				.build();
		System.out.println(cake);
	}
}
