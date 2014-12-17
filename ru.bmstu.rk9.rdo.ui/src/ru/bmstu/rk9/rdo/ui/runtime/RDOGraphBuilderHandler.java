package ru.bmstu.rk9.rdo.ui.runtime;

import javax.swing.JFrame;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ru.bmstu.rk9.rdo.ui.graph.TreeGrapher;
import ru.bmstu.rk9.rdo.ui.graph.TreeGrapher.Graph;

public class RDOGraphBuilderHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		TreeGrapher tree = new TreeGrapher();
		for (Integer key : tree.mapList.keySet()) {
			Graph frame = tree.new Graph(tree.mapList.get(key), tree.infoMap.get(key),
					tree.solutionMap.get(key));
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(800, 600);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}
		return null;
	}
}
