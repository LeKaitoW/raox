package ru.bmstu.rk9.rao.lib.event;

import java.util.PriorityQueue;
import java.util.Comparator;

import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class EventScheduler {
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

	public void pushEvent(Event event) {
		if (event.getTime() >= Simulator.getTime())
			eventList.add(event);
	}

	public Event popEvent() {
		return eventList.poll();
	}

	public boolean haveEvents() {
		return !eventList.isEmpty();
	}
}
