package ru.bmstu.rk9.rao.ui.process;

import java.util.function.Supplier;

import org.eclipse.gef.editparts.AbstractEditPart;

import ru.bmstu.rk9.rao.ui.process.node.Node;

public class ProcessNodeInfo {

	public ProcessNodeInfo(String name, Supplier<Node> nodeFactory, Supplier<AbstractEditPart> partFactory) {
		this.name = name;
		this.nodeFactory = nodeFactory;
		this.partFactory = partFactory;
	}

	private final String name;
	private final Supplier<Node> nodeFactory;
	private final Supplier<AbstractEditPart> partFactory;

	public String getName() {
		return name;
	}

	public Supplier<Node> getNodeFactory() {
		return nodeFactory;
	}

	public Supplier<AbstractEditPart> getPartFactory() {
		return partFactory;
	}
}
