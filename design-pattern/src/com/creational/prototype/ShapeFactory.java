package com.creational.prototype;

import java.util.HashMap;
import java.util.Map;

public class ShapeFactory {
	private static Map<String,Shape> cache = new HashMap<>();
	
	public static void loadInitialCache() {
		cache.clear();
		Shape circle = new Circle("circle","It is round");
		Shape rectangle = new Rectangle("rectangle","It has opposite two sides equal");
		Shape square = new Square("square","It has four sides equal");
		
		cache.put(circle.getId().toLowerCase(), circle);
		cache.put(rectangle.getId().toLowerCase(), rectangle);
		cache.put(square.getId().toLowerCase(), square);
	}
	public static void addToCache(Shape shape) {
		cache.put(shape.getId().toLowerCase(), shape);
	}
	
	//It can give null object if Shape is not registered already
	public static Shape getShape(String key) {
		return (Shape) cache.get(key.toLowerCase()).clone();
	}
	private ShapeFactory() {
	}
	
	
}
