package com.structural.bridge;

public class DashedBorder implements Border{
	
	private int thickness;

	public DashedBorder(int thickness) {
		this.thickness = thickness;
	}

	@Override
	public String getLine() {
		return "DASHED";
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
		return "DashedBorder [thickness=" + thickness + "]";
	}
	

}
