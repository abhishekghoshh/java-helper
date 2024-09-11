package com.behavioral.chainOfResponsible;

public class Client {
	public static void main(String[] args) {

		Receiver faxHandler, emailHandler;
		emailHandler = new EmailReceiver();
		faxHandler = new FaxReceiver();
		faxHandler.setNextChain(emailHandler);
		Raiser raiser = new IssueRaiser(faxHandler);

		Message m1 = new Message("Fax is reaching late to the destination", Priority.Normal);
		Message m2 = new Message("Email is not going", Priority.High);
		Message m3 = new Message("In Email, BCC field is disabled occasionally", Priority.Normal);
		Message m4 = new Message("Fax is not reaching destination", Priority.High);

		raiser.raiseMessage(m1);
		raiser.raiseMessage(m2);
		raiser.raiseMessage(m3);
		raiser.raiseMessage(m4);

	}
}
