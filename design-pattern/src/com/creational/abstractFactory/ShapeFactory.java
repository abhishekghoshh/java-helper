package com.creational.abstractFactory;

public class ShapeFactory extends AbstractFactory {

    @Override
    public Shape getShape(String shape) {
    	switch(shape.toUpperCase()) {
		case "CIRCLE":
			return new Circle();
		case "RECTANGLE":
			return new Rectangle();
		case "SQUARE":
			return new Square();
		default:
			throw new RuntimeException("Shape "+shape+" is unknown");
    	}
    }

    @Override
    public Color getColor(String color){
       	throw new RuntimeException("getColor is not Allowed");
    }

}