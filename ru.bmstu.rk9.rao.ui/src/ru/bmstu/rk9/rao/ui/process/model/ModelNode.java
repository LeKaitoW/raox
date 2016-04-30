package ru.bmstu.rk9.rao.ui.process.model;

import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.DefaultColors;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.CheckboxPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;

public class ModelNode extends Node {

	private static final long serialVersionUID = 1;

	public static String name = "Model";
	protected static final String PROPERTY_SHOW_GRID = "ShowGrid";
	protected static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";

	private transient EResourceRetriever resourceRetriever;
	private boolean showGrid = true;
	private RGB backgroundColor = DefaultColors.MODEL_BACKGROUND_COLOR.getRGB();

	public final boolean getShowGrid() {
		return showGrid;
	}

	public final void setShowGrid(boolean showGrid) {
		boolean previousValue = this.showGrid;
		this.showGrid = showGrid;
		getListeners().firePropertyChange(PROPERTY_SHOW_GRID, previousValue, showGrid);
	}

	public final RGB getBackgroundColor() {
		return backgroundColor;
	}

	public final void setBackgroundColor(RGB backgroundColor) {
		RGB previousValue = this.backgroundColor;
		this.backgroundColor = backgroundColor;
		getListeners().firePropertyChange(PROPERTY_BACKGROUND_COLOR, previousValue, backgroundColor);
	}

	public final EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}

	public final void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		properties.add(new CheckboxPropertyDescriptor(PROPERTY_SHOW_GRID, "Show Grid"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_BACKGROUND_COLOR, "Background color"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		switch (propertyName) {
		case PROPERTY_SHOW_GRID:
			return getShowGrid();

		case PROPERTY_BACKGROUND_COLOR:
			return getBackgroundColor();
		}

		return null;
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		switch (propertyName) {
		case PROPERTY_SHOW_GRID:
			setShowGrid((boolean) value);
			break;

		case PROPERTY_BACKGROUND_COLOR:
			setBackgroundColor((RGB) value);
			break;
		}
	}
}
