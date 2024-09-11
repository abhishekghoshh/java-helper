package com.creational.prototype;

public class Cube extends Shape {

	public Cube(String id,String description) {
		super.id=id;
		super.description=description;
	}
	@Override
	public void draw() {
		System.out.println("I am a 3d object");
	}

}
