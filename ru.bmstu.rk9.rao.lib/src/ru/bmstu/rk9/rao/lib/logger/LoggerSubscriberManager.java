package ru.bmstu.rk9.rao.lib.logger;

import ru.bmstu.rk9.rao.lib.logger.LoggerSubscriberManager.LoggerSubscriberInfo;
import ru.bmstu.rk9.rao.lib.notification.DefferedSubscriberManager;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class LoggerSubscriberManager extends DefferedSubscriberManager<LoggerSubscriberInfo> {
	public static class LoggerSubscriberInfo {
		public LoggerSubscriberInfo(Subscriber subscriber, Logger.NotificationCategory notificationCategory) {
			this.subscriber = subscriber;
			this.notificationCategory = notificationCategory;
		}

		final Subscriber subscriber;
		final Logger.NotificationCategory notificationCategory;
	}

	@Override
	protected void registerExecutionSubscribers() {
		for (LoggerSubscriberInfo subscriberInfo : subscribersInfo)
			CurrentSimulator.getLogger().getNotifier().addSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (LoggerSubscriberInfo subscriberInfo : subscribersInfo)
			CurrentSimulator.getLogger().getNotifier().removeSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}
}
