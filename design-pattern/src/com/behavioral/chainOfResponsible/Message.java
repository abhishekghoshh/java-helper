package com.behavioral.chainOfResponsible;

public class Message {
	private String message;
	private Priority priority;

	public Message(String message, Priority priority) {
		this.message = message;
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	
}
