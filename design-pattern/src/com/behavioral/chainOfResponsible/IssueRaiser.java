package com.behavioral.chainOfResponsible;

public class IssueRaiser implements Raiser{
	public Receiver receiver;

	public IssueRaiser(Receiver firstReceiver) {
		this.receiver = firstReceiver;
	}
	@Override
	public void raiseMessage(Message message) {
		if (receiver != null)
			receiver.processMessage(message);
	}
}
