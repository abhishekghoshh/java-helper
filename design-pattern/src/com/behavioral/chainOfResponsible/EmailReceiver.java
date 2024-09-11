package com.behavioral.chainOfResponsible;

public class EmailReceiver implements Receiver {
	private Receiver nextReceiver;

	public void setNextChain(Receiver nextReceiver) {
		this.nextReceiver = nextReceiver;
	}

	public boolean processMessage(Message message) {
		if (message.getMessage().contains("Email")) {
			System.out.println("EmailErrorHandler processed " + message.getPriority()+ "priority issue: " + message.getMessage());
			return true;
		} else {
			if (nextReceiver != null)
				nextReceiver.processMessage(message);
		}
		return false;
	}
}
