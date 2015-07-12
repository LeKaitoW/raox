package ru.bmstu.rk9.rao.lib.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationManager {
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

	protected Map<String, Subscription> subscribtions;

	public NotificationManager(List<String> categories) {
		subscribtions = new HashMap<String, Subscription>();
		for (String s : categories)
			subscribtions.put(s, new Subscription());
	}

	public void notifySubscribers(String category) {
		for (Subscriber s : subscribtions.get(category).subscribers)
			s.fireChange();
	}

	public Subscription getSubscription(String category) {
		return subscribtions.get(category);
	}

	public List<String> getAvailableSubscriptions() {
		return new ArrayList<String>(subscribtions.keySet());
	}
}
