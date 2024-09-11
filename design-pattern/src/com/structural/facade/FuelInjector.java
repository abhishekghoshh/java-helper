package com.structural.facade;

public interface FuelInjector {

	void on();

	void off();

	void inject();
}

class CarFuelInjector implements FuelInjector {
	@Override
	public void on() {

	}

	@Override
	public void inject() {

	}

	@Override
	public void off() {
	}
}
class TruckFuelInjector implements FuelInjector {
	@Override
	public void on() {

	}

	@Override
	public void inject() {

	}

	@Override
	public void off() {
	}
}

class CustomFuelInjector implements FuelInjector {
	@Override
	public void on() {

	}

	@Override
	public void inject() {

	}

	@Override
	public void off() {
	}
}
