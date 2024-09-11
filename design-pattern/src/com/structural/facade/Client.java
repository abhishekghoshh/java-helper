package com.structural.facade;

public class Client {
	public static void main(String arg[]) {
		Engine carEngine = new CarEngine();
		carEngine.start();
		carEngine.stop();

		Engine truckEngine = new TruckEngine();
		truckEngine.start();
		truckEngine.stop();

		Engine customTruckEngine = new TruckEngine(100, 60, new CustomFuelInjector(), new CustomAirFlowController(),
				new CustomStarter(), new CustomCoolingController(), new CustomCatalyticConverter());
		customTruckEngine.start();
		customTruckEngine.stop();
	}
}
