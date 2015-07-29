package ru.bmstu.rk9.rao.lib.notification;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Subscription {
	protected final Set<Subscriber> subscribers = Collections
			.newSetFromMap(new ConcurrentHashMap<Subscriber, Boolean>());

	Subscription addSubscriber(Subscriber subscriber) {
		if (!subscribers.add(subscriber))
			throw new NotifierException("Cannot add subscriber" + subscriber
					+ ", it is already present in subscription");
		return this;
	}

	void removeSubscriber(Subscriber subscriber) {
		if (!subscribers.remove(subscriber))
			throw new NotifierException("Cannot remove subscriber" + subscriber
					+ ", it is not present in subscription");
	}
}
