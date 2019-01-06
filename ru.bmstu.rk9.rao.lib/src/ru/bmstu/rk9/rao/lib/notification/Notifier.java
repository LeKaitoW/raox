package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;

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
		Subscription subscription = subscriptionState.subscription;

		for (Subscriber subscriber : subscription.subscribers.keySet()) {
			if (subscription.subscribers.get(subscriber).contains(SubscriptionType.ONE_SHOT))
				subscription.removeSubscriber(subscriber);
			subscriber.fireChange();
		}

		subscriptionState.hadNotifications = true;
	}

	public void addSubscriber(Subscriber subscriber, Category category) {
		addSubscriber(subscriber, category, EnumSet.noneOf(SubscriptionType.class));
	}

	public void addSubscriber(Subscriber subscriber, Category category, EnumSet<SubscriptionType> subscriptionFlags) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);

		subscriptionState.subscription.addSubscriber(subscriber, subscriptionFlags);

		addPostActions(subscriber, subscriptionFlags, subscriptionState);
	}

	public void addSubscriberIfNotExists(Subscriber subscriber, Category category) {
		addSubscriberIfNotExists(subscriber, category, EnumSet.noneOf(SubscriptionType.class));
	}

	public void addSubscriberIfNotExists(Subscriber subscriber, Category category,
			EnumSet<SubscriptionType> subscriptionFlags) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);

		subscriptionState.subscription.addSubscriberIfNotExists(subscriber, subscriptionFlags);

		addPostActions(subscriber, subscriptionFlags, subscriptionState);
	}

	private void addPostActions(Subscriber subscriber, EnumSet<SubscriptionType> subscriptionFlags,
			SubscriptionState subscriptionState) {
		if (!subscriptionFlags.contains(SubscriptionType.IGNORE_ACCUMULATED) && subscriptionState.hadNotifications)
			subscriber.fireChange();
	}

	public void removeSubscriber(Subscriber subscriber, Category category) {
		subscriptionStates.get(category).subscription.removeSubscriber(subscriber);
	}

	public void removeAllSubscribers(Category category) {
		subscriptionStates.get(category).subscription.subscribers.clear();
	}
}
