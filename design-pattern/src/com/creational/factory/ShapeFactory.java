package com.creational.factory;

public class ShapeFactory {

	public static Shape getShape(String shape) {
		if (null == shape)
			throw new RuntimeException("shape : " + shape + " is unknown");
		switch (shape.toUpperCase()) {
		case Shape.CIRCLE:
			return Shapes.CIRCLE;
		case Shape.RECTANGLE:
			return Shapes.RECTANGLE;
		case Shape.SQUARE:
			return Shapes.SQUARE;
		default:
			throw new RuntimeException("Shape : " + shape + " is unknown");
		}
	}

	public static Factory getShapeFactory(String shapeFactory) {
		if (null == shapeFactory)
			throw new RuntimeException("shapeFactory : " + shapeFactory + " is unknown");
		switch (shapeFactory.toUpperCase()) {
		case Shape.CIRCLE:
			return ShapeFactories.CIRCLE_FACTORY;
		case Shape.RECTANGLE:
			return ShapeFactories.RECTANGLE_FACTORY;
		case Shape.SQUARE:
			return ShapeFactories.SQUARE_FACTORY;
		default:
			throw new RuntimeException("shapeFactory : " + shapeFactory + " is unknown");
		}
	}
}
