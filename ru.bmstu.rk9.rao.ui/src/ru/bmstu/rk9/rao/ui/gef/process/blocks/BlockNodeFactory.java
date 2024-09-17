package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessEditor;

public class BlockNodeFactory implements CreationFactory {

	private Class<? extends Node> template;
	private Point lastLocation = null;

	public BlockNodeFactory(Class<? extends Node> template) {
		this.template = template;
	}

	@Override
	public Object getNewObject() {
		NodeInfo nodeInfo = ProcessEditor.getNodeInfo(template);
		if (nodeInfo == null)
			return null;
		Node node = nodeInfo.getNodeFactory().get();
		if (node instanceof BlockNode) {
			((BlockNode) node).setDropLocation(lastLocation);
		}
		return node;
		// exception, sysout template
	}

	@Override
	public Object getObjectType() {
		return template;
	}

	public void setLastLocation(Point p) {
		lastLocation = p;
	}
}
