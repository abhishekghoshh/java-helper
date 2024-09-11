package com.structural.bridge;

public class RedColor implements Color {

	private int intensity;
	
	public RedColor(int intensity) {
		this.intensity = intensity;
	}

	@Override
	public String getColor() {
		return "RED";
	}

	@Override
	public void setIntensity(int intensity) {
		this.intensity=intensity;
	}

	@Override
	public int getIntensity() {
		return intensity;
	}

	@Override
	public void showColor() {
		System.out.println("This is a Red color with intensity : "+intensity);
	}

	@Override
	public String toString() {
		return "RedColor [intensity=" + intensity + "]";
	}
}
