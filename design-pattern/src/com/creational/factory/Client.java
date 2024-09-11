package com.creational.factory;

public class Client {

	public static void main(String[] args) {

		Shapes.CIRCLE.draw();
		Shapes.RECTANGLE.draw();
		Shapes.SQUARE.draw();

		ShapeFactory.getShape(Shape.CIRCLE).draw();
		ShapeFactory.getShape(Shape.RECTANGLE).draw();
		ShapeFactory.getShape(Shape.SQUARE).draw();

		ShapeFactories.CIRCLE_FACTORY.getShape().draw();
		ShapeFactories.RECTANGLE_FACTORY.getShape().draw();
		ShapeFactories.SQUARE_FACTORY.getShape().draw();

		ShapeFactory.getShapeFactory(Shape.CIRCLE).getShape().draw();
		ShapeFactory.getShapeFactory(Shape.RECTANGLE).getShape().draw();
		ShapeFactory.getShapeFactory(Shape.SQUARE).getShape().draw();

	}
}
