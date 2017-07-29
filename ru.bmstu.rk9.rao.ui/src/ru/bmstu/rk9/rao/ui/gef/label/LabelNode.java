package ru.bmstu.rk9.rao.ui.gef.label;

import java.io.Serializable;
import java.util.List;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.DefaultColors;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;

public class LabelNode extends Node implements Serializable {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_TEXT = "Text";
	protected static final String PROPERTY_TEXT_COLOR = "Text color";
	protected static final String PROPERTY_BACKGROUND_COLOR = "Background color";
	protected static final String PROPERTY_FONT = "Font";
	protected static final String PROPERTY_VISIBLE = "Visible";
	public static String name = "Label";
	private final int border = 5;

	private String text = "text";
	private RGB textColor = DefaultColors.LABEL_TEXT_COLOR.getRGB();
	private RGB backgroundColor;
	private transient Font font;
	private boolean visible = true;

	public final String getText() {
		return text;
	}

	public final void setText(String text) {
		String previousValue = this.text;
		this.text = text;
		getListeners().firePropertyChange(PROPERTY_TEXT, previousValue, text);
	}

	public final boolean getVisible() {
		return visible;
	}

	public final void setVisible(boolean visible) {
		boolean previousValue = this.visible;
		this.visible = visible;
		getListeners().firePropertyChange(PROPERTY_VISIBLE, previousValue, visible);
	}

	public final RGB getTextColor() {
		return textColor;
	}

	public final void setTextColor(RGB textColor) {
		RGB previousValue = this.textColor;
		this.textColor = textColor;
		getListeners().firePropertyChange(PROPERTY_TEXT_COLOR, previousValue, textColor);
	}

	public final RGB getBackgroundColor() {
		if (backgroundColor != null)
			return backgroundColor;

		return ((ModelNode) getRoot()).getBackgroundColor();
	}

	public final void setBackgroundColor(RGB backgroundColor) {
		RGB previousValue = this.backgroundColor;
		if (((ModelNode) getRoot()).getBackgroundColor().equals(backgroundColor)) {
			this.backgroundColor = null;
		} else {
			this.backgroundColor = backgroundColor;
		}
		getListeners().firePropertyChange(PROPERTY_BACKGROUND_COLOR, previousValue, backgroundColor);
	}

	public final Font getFont() {
		return font;
	}

	public final void setFont(Font font) {
		Font previousValue = this.font;
		this.font = font;
		getListeners().firePropertyChange(PROPERTY_FONT, previousValue, font);
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		properties.add(new TextPropertyDescriptor(PROPERTY_TEXT, "Text"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_TEXT_COLOR, "Text color"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_BACKGROUND_COLOR, "Background color"));
		properties.add(new FontPropertyDescriptor(PROPERTY_FONT, "Font"));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_TEXT:
			return getText();

		case PROPERTY_TEXT_COLOR:
			return getTextColor();

		case PROPERTY_BACKGROUND_COLOR:
			return getBackgroundColor();

		case PROPERTY_FONT:
			return getFont();
		}
		return null;
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {

		switch (propertyName) {
		case PROPERTY_TEXT:
			setText((String) value);
			break;

		case PROPERTY_TEXT_COLOR:
			setTextColor((RGB) value);
			break;

		case PROPERTY_BACKGROUND_COLOR:
			setBackgroundColor((RGB) value);
			break;
		case PROPERTY_FONT:
			setFont((Font) value);
			break;
		}
	}

	public final Dimension getTextBounds() {
		Dimension dimension = FigureUtilities.getStringExtents(getText(), getFont());
		dimension.expand(border * 2, border * 2);
		return dimension;
	}
}
