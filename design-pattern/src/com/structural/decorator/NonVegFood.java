package com.structural.decorator;

public class NonVegFood implements Food {

	@Override
	public String getName() {
		return "non veg food";
	}

	@Override
	public String getDescription() {
		return "This is a non veg food";
	}

	@Override
	public double getPrice() {
		return 70;
	}

}
