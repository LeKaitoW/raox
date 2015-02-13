package ru.bmstu.rk9.rdo.lib;

import java.util.HashMap;
import java.util.Map;

public class NotificationManager implements Notifier
{
	protected Map<String, Notifier.Subscription> subscribtions;

	public NotificationManager(String[] categories)
	{
		subscribtions = new HashMap<String, Notifier.Subscription>();
		for(String s : categories)
			subscribtions.put(s, new Notifier.Subscription());
	}

	public void notifySubscribers(String category)
	{
		for(Subscriber s : subscribtions.get(category).subscribers)
			s.fireChange();
	}

	@Override
	public Notifier.Subscription getSubscription(String category)
	{
		return subscribtions.get(category);
	}

	@Override
	public String[] getAvailableSubscriptions()
	{
		return (String[]) subscribtions.keySet().toArray();
	}
}
