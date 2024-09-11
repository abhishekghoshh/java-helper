package com.behavioral.chainOfResponsible;

public class FaxReceiver implements Receiver {
	private Receiver nextReceiver;

	public void setNextChain(Receiver nextReceiver) {
		this.nextReceiver = nextReceiver;
	}
	@Override
	public boolean processMessage(Message message) {
		if (message.getMessage().contains("Fax")) {
			System.out.println("FaxErrorHandler processed " + message.getPriority()+ "priority issue: " + message.getMessage());
			return true;
		} else {
			if (nextReceiver != null)
				nextReceiver.processMessage(message);
		}
		return false;
	}
}