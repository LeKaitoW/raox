package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

public class NotificationManager<E extends Enum<E>> {
	public static class Subscription {
		protected LinkedList<Subscriber> subscribers;

		Subscription() {
			subscribers = new LinkedList<Subscriber>();
		}

		public Subscription addSubscriber(Subscriber object) {
			subscribers.add(object);
			return this;
		}

		public boolean removeSubscriber(Subscriber object) {
			return subscribers.remove(object);
		}
	}

	private final Map<E, Subscription> subscriptions;

	public NotificationManager(Class<E> enumClass) {
		subscriptions = new EnumMap<E, Subscription>(enumClass);

		for (E category : enumClass.getEnumConstants())
			subscriptions.put(category, new Subscription());
	}

	public void notifySubscribers(E category) {
		for (Subscriber subscriber : subscriptions.get(category).subscribers)
			subscriber.fireChange();
	}

	public Subscription getSubscription(E category) {
		return subscriptions.get(category);
	}
}
