package ru.bmstu.rk9.rao.lib.notification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Notifier<Category extends Enum<Category>> {

	private class SubscriptionState {
		private final Subscription subscription = new Subscription();
		private boolean hadNotifications = false;
	}
	private final Map<Category, SubscriptionState> subscriptionStates;

	public Notifier(Class<Category> enumClass) {
		subscriptionStates = new ConcurrentHashMap<Category, SubscriptionState>();

		for (Category category : enumClass.getEnumConstants()) {
			subscriptionStates.put(category, new SubscriptionState());
		}
	}

	public void notifySubscribers(Category category) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);

		for (Subscriber subscriber : subscriptionState.subscription.subscribers)
			subscriber.fireChange();
		subscriptionState.hadNotifications = true;
	}

	public void addSubscriber(Subscriber subscriber, Category category) {
		addSubscriber(subscriber, category, true);
	}

	public void addSubscriber(Subscriber subscriber, Category category,
			boolean notifeAboutAccumulatedData) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);

		subscriptionState.subscription.addSubscriber(subscriber);
		if (notifeAboutAccumulatedData && subscriptionState.hadNotifications)
			subscriber.fireChange();
	}

	public void removeSubscriber(Subscriber subscriber, Category category) {
		subscriptionStates.get(category).subscription.removeSubscriber(subscriber);
	}
}
