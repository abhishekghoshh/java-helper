package com.behavioral.observer;

public interface Publisher {

	void subsribe(Subscriber subsriber);

	void unsubscribe(Subscriber obj);

	void notifyObservers(String message);

	void notifyObserver(Subscriber subscriber, String message);

}
