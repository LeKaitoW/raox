package ru.bmstu.rk9.rao.lib.notification;

import java.util.LinkedList;

import ru.bmstu.rk9.rao.lib.common.Subscriber;

public interface Notifier {
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

	public Subscription getSubscription(String category);

	public String[] getAvailableSubscriptions();
}
