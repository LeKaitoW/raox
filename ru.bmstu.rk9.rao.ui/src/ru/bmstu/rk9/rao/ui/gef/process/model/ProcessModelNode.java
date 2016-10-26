package ru.bmstu.rk9.rao.ui.gef.process.model;

import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;
import ru.bmstu.rk9.rao.ui.gef.process.EResourceRetriever;

public class ProcessModelNode extends ModelNode {

	private static final long serialVersionUID = 1;

	public static String name = "Process Model";

	private transient EResourceRetriever resourceRetriever;

	public final EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}

	public final void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}
}
