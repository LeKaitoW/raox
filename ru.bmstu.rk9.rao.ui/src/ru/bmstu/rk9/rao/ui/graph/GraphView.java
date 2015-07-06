package ru.bmstu.rk9.rao.ui.graph;

import org.eclipse.swt.widgets.Menu;

import ru.bmstu.rk9.rao.lib.database.CollectedDataNode;
import ru.bmstu.rk9.rao.lib.database.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rao.ui.graph.GraphControl.FrameInfo;
import ru.bmstu.rk9.rao.ui.serialization.SerializedObjectsView.ConditionalMenuItem;

public class GraphView {

	static private class GraphMenuItem extends ConditionalMenuItem {

		public GraphMenuItem(Menu parent) {
			super(parent, "Graph");
		}

		@Override
		public boolean isEnabled(CollectedDataNode node) {
			return node.getIndex().getType() == IndexType.SEARCH;
		}

		@Override
		public void show(CollectedDataNode node) {
			int dptNumber = node.getIndex().getNumber();
			String frameName = node.getName();
			FrameInfo frameInfo = new FrameInfo(dptNumber, frameName);
			GraphControl.openFrameWindow(frameInfo);
		}
	}

	static public ConditionalMenuItem createConditionalMenuItem(Menu parent) {
		return new GraphMenuItem(parent);
	}
}
