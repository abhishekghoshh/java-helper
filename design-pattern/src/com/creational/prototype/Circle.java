package com.creational.prototype;

public class Circle extends Shape{

	
	public Circle(String id,String description) {
		super.id = id;
		super.description=description;
	}

	@Override
	public void draw() {
		System.out.println("I am a Circle");
	}
}
