package com.structural.facade;

public interface CoolingController {

	public void setTemperatureUpperLimit(int coolingTemerature);

	public void run();

	public void cool(int maxAllowedTemperature);

	public void stop();

}

class CarCoolingController implements CoolingController {

	@Override
	public void setTemperatureUpperLimit(int coolingTemerature) {

	}

	@Override
	public void run() {

	}

	@Override
	public void cool(int maxAllowedTemperature) {

	}

	@Override
	public void stop() {

	}

}
class TruckCoolingController implements CoolingController {

	@Override
	public void setTemperatureUpperLimit(int coolingTemerature) {

	}

	@Override
	public void run() {

	}

	@Override
	public void cool(int maxAllowedTemperature) {

	}

	@Override
	public void stop() {

	}

}

class CustomCoolingController implements CoolingController {

	@Override
	public void setTemperatureUpperLimit(int coolingTemerature) {

	}

	@Override
	public void run() {

	}

	@Override
	public void cool(int maxAllowedTemperature) {

	}

	@Override
	public void stop() {

	}

}
