package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

public class NotificationManager<Category extends Enum<Category>> {
	public static class Subscription {
		protected LinkedList<Subscriber> subscribers;

		Subscription() {
			subscribers = new LinkedList<Subscriber>();
		}

		public Subscription addSubscriber(Subscriber subscriber) {
			subscribers.add(subscriber);
			return this;
		}
	}

	private final Map<Category, Subscription> subscriptions;

	public NotificationManager(Class<Category> enumClass) {
		subscriptions = new EnumMap<Category, Subscription>(enumClass);

		for (Category category : enumClass.getEnumConstants())
			subscriptions.put(category, new Subscription());
	}

	public void notifySubscribers(Category category) {
		for (Subscriber subscriber : subscriptions.get(category).subscribers)
			subscriber.fireChange();
	}

	public Subscription getSubscription(Category category) {
		return subscriptions.get(category);
	}
}
