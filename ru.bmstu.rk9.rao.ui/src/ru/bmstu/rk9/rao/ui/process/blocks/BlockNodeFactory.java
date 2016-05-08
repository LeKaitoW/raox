package ru.bmstu.rk9.rao.ui.process.blocks;

import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessEditor;

public class BlockNodeFactory implements CreationFactory {

	private Class<? extends Node> template;

	public BlockNodeFactory(Class<? extends Node> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		NodeInfo nodeInfo = ProcessEditor.getNodeInfo(template);
		if (nodeInfo == null)
			return null;
		return nodeInfo.getNodeFactory().get();
		// exception, sysout template
	}

	@Override
	public Object getObjectType() {
		return template;
	}
}
