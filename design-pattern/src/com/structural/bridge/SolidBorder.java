package com.structural.bridge;

public class SolidBorder implements Border {

	private int thickness;
	
	public SolidBorder(int thickness) {
		this.thickness = thickness;
	}

	@Override
	public String getLine() {
		return "SOLID";
	}

	@Override
	public void setThickness(int thickness) {
		this.thickness=thickness;
	}

	@Override
	public int getThickness() {
		return this.thickness;
	}
	
	@Override
	public String toString() {
		return "SolidBorder [thickness=" + thickness + "]";
	}

}
