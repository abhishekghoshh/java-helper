package com.creational.factory;

public final class ShapeFactories {
	private ShapeFactories() {
	};

	public static final Factory CIRCLE_FACTORY = () -> Shapes.CIRCLE;
	public static final Factory RECTANGLE_FACTORY = () -> Shapes.RECTANGLE;
	public static final Factory SQUARE_FACTORY = () -> Shapes.SQUARE;
}
