package ru.bmstu.rk9.rao.ui.gef.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.Node;

public abstract class ModelEditPart extends EditPart {

	@Override
	public final List<Node> getModelChildren() {
		return ((Node) getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected void refreshVisuals() {
		ModelNode node = (ModelNode) getModel();
		IFigure modelBackgroundLayer = ((ScalableFreeformRootEditPart) getRoot())
				.getLayer(ModelBackgroundLayer.MODEL_BACKGROUND_LAYER);
		modelBackgroundLayer.setBackgroundColor(new Color(null, node.getBackgroundColor()));
		getViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, node.getShowGrid());
		getViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, node.getShowGrid());
		getViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, node.getShowGrid());

		getFigure().repaint();

		super.refreshVisuals();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case Node.PROPERTY_ADD:
		case Node.PROPERTY_REMOVE:
			refreshChildren();
			break;

		case ModelNode.PROPERTY_SHOW_GRID:
			refreshVisuals();
			break;

		case ModelNode.PROPERTY_BACKGROUND_COLOR:
			refreshVisuals();
			refreshChildren(children);
			break;
		}
	}

	@SuppressWarnings("rawtypes")
	private void refreshChildren(List children) {
		if (children == null)
			return;

		for (Iterator child = children.iterator(); child.hasNext();) {
			AbstractEditPart editPart = (AbstractEditPart) child.next();
			editPart.refresh();
			refreshChildren(editPart.getChildren());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == SnapToHelper.class && ((ModelNode) getModel()).getShowGrid()) {
			List<Object> snapStrategies = new ArrayList<Object>();

			Boolean snapEnabled = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
			if (snapEnabled != null && snapEnabled.booleanValue())
				snapStrategies.add(new SnapToGeometry(this));

			Boolean gridEnabled = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
			if (gridEnabled != null && gridEnabled.booleanValue())
				snapStrategies.add(new SnapToGrid(this));

			if (snapStrategies.isEmpty())
				return null;

			if (snapStrategies.size() == 1)
				return snapStrategies.get(0);

			SnapToHelper snapToHelper[] = new SnapToHelper[snapStrategies.size()];
			for (int i = 0; i < snapStrategies.size(); i++)
				snapToHelper[i] = (SnapToHelper) snapStrategies.get(i);
			return new CompoundSnapToHelper(snapToHelper);
		}
		return super.getAdapter(adapter);
	}
}
