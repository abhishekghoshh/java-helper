package com.creational.factory;

public final class Shapes {
	private Shapes() {
	}

	public static final Shape CIRCLE = () -> {
		System.out.println("inside CIRCLE draw() method");
	};
	public static final Shape RECTANGLE = () -> {
		System.out.println("inside RECTANGLE draw() method");
	};
	public static final Shape SQUARE = () -> {
		System.out.println("inside SQUARE draw() method");
	};
}
