package com.structural.facade;

public abstract class Engine {
	protected static final int DEFAULT_COOLING_TEMP = 90;
	protected static final int MAX_ALLOWED_TEMP = 50;
	protected final int maxAllowedTemperature;
	protected final int coolingTemperature;
	protected final FuelInjector fuelInjector;
	protected final AirFlowController airFlowController;
	protected final Starter starter;
	protected final CoolingController coolingController;
	protected final CatalyticConverter catalyticConverter;

	public Engine(int maxAllowedTemperature, int coolingTemperature, FuelInjector fuelInjector,
			AirFlowController airFlowController, Starter starter, CoolingController coolingController,
			CatalyticConverter catalyticConverter) {
		this.maxAllowedTemperature = maxAllowedTemperature;
		this.coolingTemperature = coolingTemperature;
		this.fuelInjector = fuelInjector;
		this.airFlowController = airFlowController;
		this.starter = starter;
		this.coolingController = coolingController;
		this.catalyticConverter = catalyticConverter;
	}

	public void start() {
		fuelInjector.on();
		airFlowController.takeAir();
		fuelInjector.inject();
		starter.start();
		coolingController.setTemperatureUpperLimit(coolingTemperature);
		coolingController.run();
		catalyticConverter.on();
	}

	public void stop() {
		fuelInjector.off();
		catalyticConverter.off();
		coolingController.cool(maxAllowedTemperature);
		coolingController.stop();
		airFlowController.off();
	}

}

class CarEngine extends Engine {
	public CarEngine() {
		super(DEFAULT_COOLING_TEMP, MAX_ALLOWED_TEMP, new CarFuelInjector(), new CarAirFlowController(),
				new CarStarter(), new CarCoolingController(), new CarCatalyticConverter());
	}

}

class TruckEngine extends Engine {
	public TruckEngine() {
		super(DEFAULT_COOLING_TEMP, MAX_ALLOWED_TEMP, new TruckFuelInjector(), new TruckAirFlowController(),
				new TruckStarter(), new TruckCoolingController(), new TruckCatalyticConverter());
	}

	public TruckEngine(int maxAllowedTemperature, int coolingTemperature, FuelInjector fuelInjector,
			AirFlowController airFlowController, Starter starter, CoolingController coolingController,
			CatalyticConverter catalyticConverter) {
		super(maxAllowedTemperature, coolingTemperature, fuelInjector, airFlowController, starter, coolingController,
				catalyticConverter);
	}
}