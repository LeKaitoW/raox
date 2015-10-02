package ru.bmstu.rk9.rao.ui.graph;

import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class GraphUtil {
	public static final mxCell getSelectionVertex(mxGraph graph) {
		if (graph.getSelectionCell() == null)
			return null;

		mxCell cell = (mxCell) graph.getSelectionCell();

		if (!cell.isVertex())
			return null;

		if (!(cell.getValue() instanceof Node))
			return null;

		return cell;
	}
}
