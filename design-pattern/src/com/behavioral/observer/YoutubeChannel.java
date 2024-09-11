package com.behavioral.observer;

import java.util.HashSet;
import java.util.Set;

public class YoutubeChannel implements Publisher {

	private final String name;
	private final Set<Subscriber> subscribers;

	public YoutubeChannel(String name) {
		this.name = name;
		this.subscribers = new HashSet<>();
	}

	@Override
	public void subsribe(Subscriber subsriber) {
		if (subsriber == null)
			throw new NullPointerException("Null Subscriber");
		subscribers.add(subsriber);
	}

	@Override
	public void unsubscribe(Subscriber obj) {
		subscribers.remove(obj);
	}

	@Override
	public void notifyObservers(String message) {
		if (message != null) {
			for (Subscriber subscriber : subscribers)
				subscriber.receive(name + " has sent this message : " + message);
		}
	}

	@Override
	public void notifyObserver(Subscriber subscriber, String message) {
		if (null != message && subscribers.contains(subscriber))
			subscriber.receive(name + " has sent this message : " + message);
	}

}
