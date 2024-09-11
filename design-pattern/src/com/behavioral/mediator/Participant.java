package com.behavioral.mediator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class Participant {
	protected Mediator mediator;
	protected String name;

	protected static DateFormat dateFormat = new SimpleDateFormat("E dd-MM-yyyy hh:mm a");

	public Participant(Mediator mediator, String name) {
		this.mediator = mediator;
		this.name = name;
	}

	public abstract void send(String message);

	public abstract void receive(String message);
}
