package ru.bmstu.rk9.rao.ui.graph;

import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.ui.graph.GraphPanel.GraphEvent;

import com.mxgraph.view.mxGraph;

public interface GraphApi {
	public mxGraph getGraph();

	public GraphInfoWindow getGraphInfoWindow();

	public Notifier<GraphEvent> getGraphEventNotifier();
}
