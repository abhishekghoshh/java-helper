package com.structural.bridge;

public interface Shape {
	void setColor(Color color);
	Color getColor();
	void setBorder(Border border);
	Border getBorder();
	void description();
	String toString();
}
