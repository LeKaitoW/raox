package ru.bmstu.rk9.rao.ui.gef.model;

import java.util.List;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.CheckboxPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.gef.DefaultColors;
import ru.bmstu.rk9.rao.ui.gef.DefaultFonts;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.SerializableFont;
import ru.bmstu.rk9.rao.ui.gef.label.FontPropertyDescriptor;

public class ModelNode extends Node {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_SHOW_GRID = "ShowGrid";
	protected static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
	protected static final String PROPERTY_GLOBAL_FONT = "GlobalFont";

	private boolean showGrid = true;
	private RGB backgroundColor = DefaultColors.MODEL_BACKGROUND_COLOR.getRGB();
	private SerializableFont globalFont = DefaultFonts.DEFAULT_FONT;

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

	public final Font getGlobalFont() {
		// В старых версиях после десериализации данное поле может быть null,
		// тогда устанавливаем дефолтное
		if (globalFont == null) {
			globalFont = DefaultFonts.DEFAULT_FONT;
		}
		return globalFont.getFont();
	}

	public final void setGlobalFont(Font font) {
		Font previousValue = getGlobalFont();
		this.globalFont = new SerializableFont(font);
		getListeners().firePropertyChange(PROPERTY_GLOBAL_FONT, previousValue, font);
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		properties.add(new CheckboxPropertyDescriptor(PROPERTY_SHOW_GRID, "Show Grid"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_BACKGROUND_COLOR, "Background color"));
		properties.add(new FontPropertyDescriptor(PROPERTY_GLOBAL_FONT, "Global font"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		switch (propertyName) {
		case PROPERTY_SHOW_GRID:
			return getShowGrid();

		case PROPERTY_BACKGROUND_COLOR:
			return getBackgroundColor();

		case PROPERTY_GLOBAL_FONT:
			return getGlobalFont();
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

		case PROPERTY_GLOBAL_FONT:
			setGlobalFont((Font) value);
			break;
		}
	}
}
