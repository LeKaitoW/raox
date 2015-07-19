package ru.bmstu.rk9.rao.lib.notification;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Subscription {
	protected final Set<Subscriber> subscribers = Collections
			.newSetFromMap(new ConcurrentHashMap<Subscriber, Boolean>());

	Subscription addSubscriber(Subscriber subscriber) {
		subscribers.add(subscriber);
		return this;
	}

	void removeSubscriber(Subscriber subscriber) {
		subscribers.remove(subscriber);
	}
}
