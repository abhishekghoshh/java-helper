package com.creational.prototype;

public class Client {

	public static void main(String[] args) {

		ShapeFactory.loadInitialCache();
		Shape circle = ShapeFactory.getShape("circle");
		circle.draw();
		Shape rectangle = ShapeFactory.getShape("rectangle");
		rectangle.draw();
		Shape square = ShapeFactory.getShape("square");
		square.draw();
		
		Shape cube = new Cube("cube","I am a cube");
		ShapeFactory.addToCache(cube);
		
		Shape clonedCube = ShapeFactory.getShape("cube");
		clonedCube.draw();
		
		System.out.println(cube == clonedCube);
	}

}
