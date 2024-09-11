package com.behavioral.state;

public class AlertStateContext {
	private State currentState;

	public AlertStateContext() {
		currentState = new Ringing(); 
	}

	public void setState(State state) {
		currentState = state;
	}

	public void alert() {
		currentState.alert();
	}
}
