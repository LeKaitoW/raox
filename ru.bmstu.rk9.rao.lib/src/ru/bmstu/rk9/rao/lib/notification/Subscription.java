package ru.bmstu.rk9.rao.lib.notification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Subscription {
	public enum SubscriptionType {
		REGULAR, ONE_SHOT, IGNORE_ACCUMULATED
	};

	protected final Map<Subscriber, SubscriptionType> subscribers = new ConcurrentHashMap<>();

	Subscription addSubscriber(Subscriber subscriber, SubscriptionType type) {
		if (subscribers.put(subscriber, type) != null)
			throw new NotifierException("Cannot add subscriber" + subscriber
					+ ", it is already present in subscription");
		return this;
	}

	void removeSubscriber(Subscriber subscriber) {
		if (subscribers.remove(subscriber) == null)
			throw new NotifierException("Cannot remove subscriber" + subscriber
					+ ", it is not present in subscription");
	}
}
