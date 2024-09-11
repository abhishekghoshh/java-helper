package com.structural.bridge;

public class BlueColor implements Color{
	
	private int intensity;
	
	public BlueColor(int intensity) {
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
		System.out.println("This is a Blue color with intensity : "+intensity);
	}

	@Override
	public String toString() {
		return "BlueColor [intensity=" + intensity + "]";
	}
}
