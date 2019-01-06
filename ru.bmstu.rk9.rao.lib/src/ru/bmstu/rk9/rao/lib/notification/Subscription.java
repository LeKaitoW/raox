package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Subscription {
	public enum SubscriptionType {
		ONE_SHOT, IGNORE_ACCUMULATED
	};

	protected final Map<Subscriber, Set<SubscriptionType>> subscribers = new ConcurrentHashMap<>();

	void addSubscriber(Subscriber subscriber, EnumSet<SubscriptionType> flags) {
		if (subscribers.put(subscriber, flags) != null)
			throw new NotifierException(
					"Cannot add subscriber" + subscriber + ", it is already present in subscription");
	}

	void addSubscriberIfNotExists(Subscriber subscriber, EnumSet<SubscriptionType> flags) {
		if (!subscribers.containsKey(subscriber)) {
			addSubscriber(subscriber, flags);
		}
	}

	void removeSubscriber(Subscriber subscriber) {
		if (subscribers.remove(subscriber) == null)
			throw new NotifierException(
					"Cannot remove subscriber" + subscriber + ", it is not present in subscription");
	}
}
