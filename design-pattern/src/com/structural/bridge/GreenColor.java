package com.structural.bridge;

public class GreenColor implements Color{
	private int intensity;
	
	public GreenColor(int intensity) {
		this.intensity = intensity;
	}

	@Override
	public String getColor() {
		return "GREEN";
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
		System.out.println("This is a Green color with intensity : "+intensity);
	}

	@Override
	public String toString() {
		return "GreenColor [intensity=" + intensity + "]";
	}
}
