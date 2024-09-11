package com.structural.facade;

public interface AirFlowController {

	void takeAir();

	void off();

}

class CarAirFlowController implements AirFlowController {

	@Override
	public void takeAir() {

	}

	@Override
	public void off() {

	}

}
class TruckAirFlowController implements AirFlowController {

	@Override
	public void takeAir() {

	}

	@Override
	public void off() {

	}

}
class CustomAirFlowController implements AirFlowController {

	@Override
	public void takeAir() {

	}

	@Override
	public void off() {

	}

}