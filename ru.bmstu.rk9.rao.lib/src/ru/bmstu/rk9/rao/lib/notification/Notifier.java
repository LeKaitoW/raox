package ru.bmstu.rk9.rao.lib.notification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Notifier<Category extends Enum<Category>> {
	private final Map<Category, Subscription> subscriptions;
	private final Map<Category, Boolean> hadNotifications;

	public Notifier(Class<Category> enumClass) {
		subscriptions = new ConcurrentHashMap<Category, Subscription>();
		hadNotifications = new ConcurrentHashMap<Category, Boolean>();

		for (Category category : enumClass.getEnumConstants()) {
			subscriptions.put(category, new Subscription());
			hadNotifications.put(category, false);
		}
	}

	public void notifySubscribers(Category category) {
		for (Subscriber subscriber : subscriptions.get(category).subscribers)
			subscriber.fireChange();
		hadNotifications.put(category, true);
	}

	public void addSubscriber(Subscriber subscriber, Category category) {
		addSubscriber(subscriber, category, true);
	}

	public void addSubscriber(Subscriber subscriber, Category category,
			boolean notifeAboutOldData) {
		subscriptions.get(category).addSubscriber(subscriber);
		if (notifeAboutOldData && hadNotifications.get(category))
			subscriber.fireChange();
	}

	public void removeSubscriber(Subscriber subscriber, Category category) {
		subscriptions.get(category).removeSubscriber(subscriber);
	}
}
