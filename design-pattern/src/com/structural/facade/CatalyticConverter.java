package com.structural.facade;

public interface CatalyticConverter {

	void on();

	void off();

}

class CarCatalyticConverter implements CatalyticConverter {

	@Override
	public void on() {
	}

	@Override
	public void off() {

	}

}

class TruckCatalyticConverter implements CatalyticConverter {

	@Override
	public void on() {
	}

	@Override
	public void off() {

	}

}

class CustomCatalyticConverter implements CatalyticConverter {

	@Override
	public void on() {
	}

	@Override
	public void off() {

	}

}
