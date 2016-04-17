package ru.bmstu.rk9.rao.ui.process.model;

import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class ModelNode extends Node {

	private static final long serialVersionUID = 1;

	public static String name = "Model";
	private transient EResourceRetriever resourceRetriever;

	public final EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}

	public final void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}
}
