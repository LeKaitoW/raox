package rdo.game5;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.json.simple.JSONArray;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.ui.graph.GraphPanel;
import ru.bmstu.rk9.rao.ui.graph.GraphPanel.GraphEvent;
import ru.bmstu.rk9.rao.ui.graph.TreeBuilder.Node;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraphSelectionModel;

public class GraphManager {

	public GraphManager(GraphPanel graph, JSONArray order) {
		this.graphPanel = graph;
		this.initialOrder = order;
		this.currentOrder = order;
		this.graphPanel.getGraph().getSelectionModel()
				.addListener(mxEvent.CHANGE, selectionListener);
		graph.getGraphEventNotifier().addSubscriber(addBoardSubscriber,
				GraphEvent.GRAPHINFO_WINDOW_OPENED);
	}

	private final int tilesCountX = 3;
	private final int tilesCountY = 2;
	private final List<TileButton> tiles = new ArrayList<>();

	private final Subscriber addBoardSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			Composite infoArea = graphPanel.getGraphInfoWindow().getInfoArea();
			Group boardGroup = new Group(infoArea, SWT.NONE);
			GridLayout boardLayout = new GridLayout(3, false);
			boardGroup.setLayout(boardLayout);
			boardGroup.setText("Board");
			createBoard(boardGroup);
		}
	};

	private final GraphPanel graphPanel;
	private final JSONArray initialOrder;
	private JSONArray currentOrder;

	private final mxIEventListener selectionListener = new mxIEventListener() {

		@Override
		public void invoke(Object sender, mxEventObject evt) {
			final mxGraphSelectionModel mxGraphSelectionModel = (mxGraphSelectionModel) sender;
			final mxCell mxCell = (mxCell) mxGraphSelectionModel.getCell();
			if (mxCell == null)
				return;
			if (!(mxCell.getValue() instanceof Node)) {
				return;
			}
			final Node node = (Node) mxCell.getValue();
			final List<String> rules = createRulesList(node);
			currentOrder = useRules(rules);
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				updateBoard();
			});
		}
	};

	private final List<String> createRulesList(final Node node) {
		Node currentNode = node;
		final List<String> rules = new ArrayList<String>();
		while (currentNode.parent != null) {
			rules.add(currentNode.ruleName);
			currentNode = currentNode.parent;
		}
		return rules;
	}

	@SuppressWarnings("unchecked")
	private final JSONArray useRules(List<String> rules) {
		final JSONArray nodeOrder = (JSONArray) initialOrder.clone();
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

	private final void createBoard(final Group boardGroup) {
		tiles.clear();
		for (int i = 0; i < tilesCountX * tilesCountY; i++) {
			tiles.add(new TileButton(boardGroup, SWT.NONE, currentOrder.get(i)
					.toString(), i + 1));
		}
	}

	private final void updateBoard() {
		if (graphPanel.graphInfoAccessible()) {
			for (int i = 0; i < tilesCountX * tilesCountY; i++) {
				tiles.get(i).updateTile(currentOrder.get(i).toString());
			}
		}
	}
}
