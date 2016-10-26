package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Queue implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = () -> getCurrentTransact();
	private TransactQueue queue;
	private final int capacity;

	public static enum QueueAction {
		ADD("added"), REMOVE("removed");

		private QueueAction(final String action) {
			this.action = action;
		}

		public String getString() {
			return action;
		}

		private final String action;
	}

	public Transact getCurrentTransact() {
		Transact outputTransact = queue.peek();
		if (outputTransact != null) {
			queue.remove();
			addQueueEntryToDatabase(outputTransact, QueueAction.REMOVE);
			return outputTransact;
		}
		return null;
	}

	public enum Queueing {
		FIFO, LIFO
	};

	public Queue(int capacity, Queueing queueing) {
		this.capacity = capacity;
		switch (queueing) {
		case FIFO:
			queue = new FIFOQueue();
			break;
		case LIFO:
			queue = new LIFOQueue();
			break;
		}
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		Transact inputTransact = inputDock.pullTransact();
		if (inputTransact != null) {
			if (queue.size() < capacity) {
				queue.offer(inputTransact);
				addQueueEntryToDatabase(inputTransact, QueueAction.ADD);
				return BlockStatus.SUCCESS;
			} else
				return BlockStatus.CHECK_AGAIN;
		}
		return BlockStatus.NOTHING_TO_DO;
	}

	private void addQueueEntryToDatabase(Transact transact, QueueAction queueAction) {
		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		data.put((byte) queueAction.ordinal());
		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.QUEUE, transact.getNumber(), data);
	}
}

class FIFOQueue implements TransactQueue {

	private LinkedList<Transact> queue = new LinkedList<Transact>();

	@Override
	public boolean offer(Transact transact) {
		return queue.offerLast(transact);
	}

	@Override
	public Transact remove() {
		return queue.removeFirst();
	}

	@Override
	public Transact peek() {
		return queue.peekFirst();
	}

	@Override
	public int size() {
		return queue.size();
	}
}

class LIFOQueue implements TransactQueue {

	private LinkedList<Transact> queue = new LinkedList<Transact>();

	@Override
	public boolean offer(Transact transact) {
		return queue.offerLast(transact);
	}

	@Override
	public Transact remove() {
		return queue.removeLast();
	}

	@Override
	public Transact peek() {
		return queue.peekLast();
	}

	@Override
	public int size() {
		return queue.size();
	}
}
