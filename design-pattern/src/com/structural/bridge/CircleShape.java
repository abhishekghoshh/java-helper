package com.structural.bridge;

public class CircleShape implements Shape{
	private Color color;
	private Border border;
	
	public CircleShape(Color color, Border border) {
		this.color = color;
		this.border = border;
	}

	@Override
	public void setColor(Color color) {
		this.color=color;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setBorder(Border border) {
		this.border=border;
	}

	@Override
	public Border getBorder() {
		return border;
	}

	@Override
	public void description() {
		getColor().showColor();
		System.out.println(this.toString());
	}

	@Override
	public String toString() {
		return "CircleShape [color=" + color + ", border=" + border + "]";
	}
}
