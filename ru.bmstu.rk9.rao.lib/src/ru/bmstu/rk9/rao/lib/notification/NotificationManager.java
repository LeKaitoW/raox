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

	private final Map<E, Subscription> sub;

	public NotificationManager(Class<E> enumClass) {
		sub = new EnumMap<E, Subscription>(enumClass);

		for (E s : enumClass.getEnumConstants())
			sub.put(s, new Subscription());
	}

	public void notifySubscribers(E category) {
		for (Subscriber s : sub.get(category).subscribers)
			s.fireChange();
	}

	public Subscription getSubscription(E category) {
		return sub.get(category);
	}
}
