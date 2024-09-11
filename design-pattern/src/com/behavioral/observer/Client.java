package com.behavioral.observer;

public class Client {
	public static void main(String[] args) {
		YoutubeChannel youtubeChannel1 = new YoutubeChannel("youtube-channel-1");
		YoutubeChannel youtubeChannel2 = new YoutubeChannel("youtube-channel-2");

		Subscriber subscriber1 = new YoutubeSubscriber("subscriber-1");
		Subscriber subscriber2 = new YoutubeSubscriber("subscriber-2");
		Subscriber subscriber3 = new YoutubeSubscriber("subscriber-3");

		youtubeChannel1.subsribe(subscriber1);
		youtubeChannel1.subsribe(subscriber2);
		youtubeChannel1.subsribe(subscriber3);

		youtubeChannel2.subsribe(subscriber1);
		youtubeChannel2.subsribe(subscriber2);

		youtubeChannel1.notifyObservers("A video has been posted");
		youtubeChannel1.notifyObserver(subscriber2, "We will visit your city soon");
		youtubeChannel2.notifyObservers("I had taken a break from youtube");

		subscriber2.printNotification();
		subscriber2.printNotification();

	}
}
