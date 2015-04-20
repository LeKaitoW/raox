package ru.bmstu.rk9.rdo.lib;

import java.util.PriorityQueue;
import java.util.Comparator;

class EventScheduler {
	private static Comparator<Event> comparator = new Comparator<Event>() {
		@Override
		public int compare(Event x, Event y) {
			if (x.getTime() < y.getTime())
				return -1;
			if (x.getTime() > y.getTime())
				return 1;
			return 0;
		}
	};

	private PriorityQueue<Event> eventList = new PriorityQueue<Event>(1,
			comparator);

	void pushEvent(Event event) {
		if (event.getTime() >= Simulator.getTime())
			eventList.add(event);
	}

	Event popEvent() {
		return eventList.poll();
	}

	boolean haveEvents() {
		return !eventList.isEmpty();
	}
}
