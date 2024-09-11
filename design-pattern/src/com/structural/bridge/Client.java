package com.structural.bridge;

public class Client {
	
	public static void main(String args[]) {
		Shape shape =null;
		Color color =null;
		Border border=null;
		
		color = new RedColor(5);
		border = new DashedBorder(9);
		shape = new CircleShape(color,border);
		shape.description();
		
		color = new BlueColor(10);
		border = new SolidBorder(10);
		shape = new RectangleShape(color,border);
		shape.description();
	}
}
