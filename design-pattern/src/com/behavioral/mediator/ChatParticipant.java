package com.behavioral.mediator;

import java.util.Date;

public class ChatParticipant extends Participant {

	public ChatParticipant(Mediator mediator, String name) {
		super(mediator, name);
	}

	@Override
	public void send(String message) {
		System.out.println(dateFormat.format(new Date()).toString()+" : "+this.name + " : Sending Message : " + message);
		mediator.sendMessage(message, this);
	}

	@Override
	public void receive(String message) {
		System.out.println(dateFormat.format(new Date()).toString()+" : "+this.name + " : Received Message : " + message);
	}

}
