package com.behavioral.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class YoutubeSubscriber implements Subscriber {

	private final Stack<String> notification;
	private String name;

	public YoutubeSubscriber(String name) {
		this.name = name;
		this.notification = new Stack<>();
	}

	public YoutubeSubscriber() {
		this.name = "anynomous";
		this.notification = new Stack<>();
	}

	@Override
	public boolean hasNotification() {
		return !this.notification.isEmpty();
	}

	@Override
	public void receive(String message) {
		this.notification.push(message);
	}

	@Override
	public String getNotification() {
		if (notification.isEmpty())
			return null;
		return notification.pop();
	}

	@Override
	public List<String> getAllNotification() {
		List<String> notification = new ArrayList<>();
		if (this.notification.isEmpty())
			return notification;
		while (!this.notification.isEmpty())
			notification.add(this.notification.pop());
		return notification;
	}

	@Override
	public void printNotification() {
		if (this.hasNotification()) {
			System.out.println(this.name + " has this notification list ");
			for (String notification : this.getAllNotification())
				System.out.println(notification);
		} else {
			System.out.println(this.name + " has no new notification ");
		}
	}

}
