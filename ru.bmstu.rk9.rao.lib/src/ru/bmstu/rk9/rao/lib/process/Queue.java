package ru.bmstu.rk9.rao.lib.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Queue implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = () -> getCurrentTransact();
	private TransactQueue queue;
	private final int capacity;

	public Transact getCurrentTransact() {
		Transact outputTransact = queue.peek();
		if (outputTransact != null) {
			queue.remove();
			System.out.println(Simulator.getTime() + " queue removed " + outputTransact.getNumber());
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
				System.out.println(Simulator.getTime() + " queue added " + inputTransact.getNumber());
				return BlockStatus.SUCCESS;
			} else
				return BlockStatus.CHECK_AGAIN;
		}
		return BlockStatus.NOTHING_TO_DO;
	}

	public static String[] getQueueingArray() {
		List<String> queueingList = new ArrayList<String>();
		for (Queueing queueing : Queueing.values())
			queueingList.add(queueing.toString());
		return queueingList.stream().toArray(String[]::new);
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
