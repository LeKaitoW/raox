package rdo.game5;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;

import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;

public class GraphManager {

	public GraphManager(mxGraph graph, JSONArray order) {
		this.graph = graph;
		this.graph.getSelectionModel().addListener(mxEvent.CHANGE, selectionListener);
		this.order = order;
	}

	private mxGraph graph;
	private JSONArray order;

	private final mxIEventListener selectionListener = new mxIEventListener() {

		@Override
		public void invoke(Object sender, mxEventObject evt) {
			final mxGraphSelectionModel mxGraphSelectionModel = (mxGraphSelectionModel) sender;
			final mxCell mxCell = (mxCell) mxGraphSelectionModel.getCell();
			final Node node = (Node) mxCell.getValue();
			drawBoard(node);
		}
	};

	@SuppressWarnings("unchecked")
	private final void drawBoard(final Node node) {
		final List<String> rules = createRulesList(node);
		final JSONArray nodeOrder = useRules(rules);
		nodeOrder.set(nodeOrder.indexOf("6"), null);
		for (int i = 0; i < nodeOrder.size(); i++) {
			if (i < 3)
				graph.insertVertex(graph.getDefaultParent(), null, nodeOrder.get(i), 10 + i * 20, 70, 20, 20);
			else
				graph.insertVertex(graph.getDefaultParent(), null, nodeOrder.get(i), 10 + (i - 3) * 20, 90, 20, 20);
		}
	}

	private final List<String> createRulesList(final Node node) {
		Node currentNode = node;
		final List<String> rules = new ArrayList<String>();
		while (currentNode.parent != null) {
			final String rule = currentNode.ruleDesсription;
			rules.add(rule.substring(0, rule.indexOf("(")));
			currentNode = currentNode.parent;
		}
		return rules;
	}

	@SuppressWarnings("unchecked")
	private final JSONArray useRules(List<String> rules) {
		final JSONArray nodeOrder = (JSONArray) order.clone();
		for (int i = rules.size() - 1; i >= 0; i--) {
			final int holeIndex = nodeOrder.indexOf("6");
			switch (rules.get(i)) {
			case "Перемещение_влево":
				nodeOrder.set(holeIndex, nodeOrder.get(holeIndex + 1));
				nodeOrder.set(holeIndex + 1, "6");
				break;
			case "Перемещение_вправо":
				nodeOrder.set(holeIndex, nodeOrder.get(holeIndex - 1));
				nodeOrder.set(holeIndex - 1, "6");
				break;
			case "Перемещение_вверх":
				nodeOrder.set(holeIndex, nodeOrder.get(holeIndex + 3));
				nodeOrder.set(holeIndex + 3, "6");
				break;
			case "Перемещение_вниз":
				nodeOrder.set(holeIndex, nodeOrder.get(holeIndex - 3));
				nodeOrder.set(holeIndex - 3, "6");
				break;
			default:
				break;
			}
		}
		return nodeOrder;
	}
}
