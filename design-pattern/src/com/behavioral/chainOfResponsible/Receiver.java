package com.behavioral.chainOfResponsible;

public interface Receiver {
	boolean processMessage(Message msg);
	void setNextChain(Receiver nextChain);
}

