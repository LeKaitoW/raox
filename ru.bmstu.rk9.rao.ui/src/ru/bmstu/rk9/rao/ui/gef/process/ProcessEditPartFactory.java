package ru.bmstu.rk9.rao.ui.gef.process;

import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.requests.CreateRequest;

import com.google.common.base.Supplier;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelEditPart;

public class ProcessEditPartFactory implements EditPartFactory {

	private int currentID = 0;
	private ProcessModelEditPart modelEditPart;
	Supplier<Map<Node, EditPart>> registry;

	public ProcessEditPartFactory(Supplier<Map<Node, EditPart>> registry) {
		this.registry = registry;
	}

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof Connection) {
			AbstractEditPart editPart = new ConnectionEditPart();
			editPart.setModel(model);
			return editPart;
		}

		@SuppressWarnings("unchecked")
		NodeInfo nodeInfo = ProcessEditor.getNodeInfo((Class<? extends Node>) model.getClass());
		if (nodeInfo == null)
			return null;

		AbstractEditPart editPart = nodeInfo.getEditPartFactory().get();
		editPart.setModel(model);

		if (editPart instanceof ProcessModelEditPart) {
			modelEditPart = (ProcessModelEditPart) editPart;
			return editPart;
		}

		if (editPart instanceof BlockEditPart) {
			BlockEditPart blockEditPart = (BlockEditPart) editPart;
			BlockNode blockNode = (BlockNode) model;
			blockEditPart.setID(currentID);
			blockNode.setID(currentID);
			processAutoConnection(blockNode, blockEditPart);
			currentID++;
		}

		if (editPart instanceof BlockTitleEditPart) {
			((BlockTitleNode) model).setFont(modelEditPart.getFigure().getFont());
		}

		return editPart;
	}

	private void processAutoConnection(BlockNode currentNode, BlockEditPart currentEditPart) {
		if (!currentEditPart.haveInputs())
			return;

		Map<Node, EditPart> nodes = registry.get();

		for (Map.Entry<Node, EditPart> entry : nodes.entrySet()) {
			if (entry.getKey() instanceof BlockNode && entry.getValue() instanceof BlockEditPart) {
				BlockNode key = (BlockNode) entry.getKey();
				BlockEditPart value = (BlockEditPart) entry.getValue();

				if (!value.haveOutputs() || value.isConnected())
					continue;

				Rectangle bounds = value.getProcessFigure().getBounds();
				Point dropLocation = currentNode.getDropLocation();
				if (isNear(bounds, dropLocation)) {
					CreateRequest request = new CreateRequest();
					request.setLocation(dropLocation);

					String sourceDockName = value.mapRequestToSourceDock(request);
					String targetDockName = currentEditPart.mapRequestToTargetDock(request);
					Connection connection = new Connection(key, currentNode, sourceDockName, targetDockName);
					connection.connect();
					break;
				}
			}
		}
	}

	private boolean isNear(Rectangle rect, Point p) {
		rect.x -= rect.width * 2;
		rect.y -= rect.height * 2;
		rect.width *= 5;
		rect.height *= 5;

		return rect.contains(p);
	}
}
