package ru.bmstu.rk9.rao.ui.process.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.ScalableRootEditPart;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.process.ProcessGridLayer;
import ru.bmstu.rk9.rao.ui.process.ProcessLayoutEditPolicy;

public class ModelEditPart extends EditPart {

	@Override
	protected IFigure createFigure() {
		return ((ScalableRootEditPart) getRoot()).getLayer(ProcessEditor.MODEL_LAYER);
	}

	@Override
	public final List<Node> getModelChildren() {
		return ((Node) getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		ModelNode node = (ModelNode) getModel();
		ProcessGridLayer.getProcessGridLayer(this).setShowGrid(node.getShowGrid());
		getViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, node.getShowGrid());
		getViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, node.getShowGrid());

		getFigure().repaint();
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
