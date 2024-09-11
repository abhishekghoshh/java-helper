package com.structural.decorator;

public class FriedRice extends FoodDecorator {

	public FriedRice(Food food) {
		super(food);
	}
	
	public String getName() {
		return super.getName()+" Fried Rice";
	}

	@Override
	public String getDescription() {
		return super.getDescription()+ " and it is fried rice";
	}

	@Override
	public double getPrice() {
		return super.getPrice()*1.5;
	}
}
