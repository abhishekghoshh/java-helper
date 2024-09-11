package com.creational.prototype;

public class Square extends Shape{
	public Square(String id,String description) {
		super.id = id;
		super.description=description;
	}

	@Override
	public void draw() {
		System.out.println("I am a Square");
	}
}
