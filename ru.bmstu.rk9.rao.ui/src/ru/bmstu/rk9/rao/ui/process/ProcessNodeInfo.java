package ru.bmstu.rk9.rao.ui.process;

import java.util.function.Supplier;

public class ProcessNodeInfo {

	public ProcessNodeInfo(String name, Supplier<Node> nodeFactory,
			Supplier<ProcessEditPart> partFactory) {
		this.name = name;
		this.nodeFactory = nodeFactory;
		this.partFactory = partFactory;
	}

	private final String name;
	private final Supplier<Node> nodeFactory;
	private final Supplier<ProcessEditPart> partFactory;

	public String getName() {
		return name;
	}

	public Supplier<Node> getNodeFactory() {
		return nodeFactory;
	}

	public Supplier<ProcessEditPart> getPartFactory() {
		return partFactory;
	}
}
