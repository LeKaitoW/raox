package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.swt.widgets.Menu;

import ru.bmstu.rk9.rdo.lib.CollectedDataNode;
import ru.bmstu.rk9.rdo.lib.CollectedDataNode.IndexType;
import ru.bmstu.rk9.rdo.ui.contributions.SerializedObjectsView.ConditionalMenuItem;
import ru.bmstu.rk9.rdo.ui.graph.GraphControl;
import ru.bmstu.rk9.rdo.ui.graph.GraphControl.FrameInfo;

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
