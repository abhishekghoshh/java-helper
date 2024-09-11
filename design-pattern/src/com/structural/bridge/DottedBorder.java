package com.structural.bridge;

public class DottedBorder implements Border{
	private int thickness;

	public DottedBorder(int thickness) {
		this.thickness = thickness;
	}
	
	@Override
	public String getLine() {
		return "DOTTED";
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
		return "DottedBorder [thickness=" + thickness + "]";
	}
	


}
