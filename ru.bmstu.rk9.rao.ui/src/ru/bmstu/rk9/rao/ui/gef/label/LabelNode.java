package ru.bmstu.rk9.rao.ui.gef.label;

import java.io.Serializable;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.DefaultColors;
import ru.bmstu.rk9.rao.ui.gef.Node;

public class LabelNode extends Node implements Serializable {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_TEXT = "Text";
	protected static final String PROPERTY_TEXT_COLOR = "Text color";
	protected static final String PROPERTY_BACKGROUND_COLOR = "Background color";
	public static String name = "Label";

	private String text;
	private RGB textColor;
	private RGB backgroundColor;

	public LabelNode() {
		text = "text";
		textColor = DefaultColors.LABEL_TEXT_COLOR.getRGB();
		backgroundColor = DefaultColors.LABEL_BACKGROUND_COLOR.getRGB();
	}

	public final String getText() {
		return text;
	}

	public final void setText(String text) {
		String previousValue = this.text;
		this.text = text;
		getListeners().firePropertyChange(PROPERTY_TEXT, previousValue, text);
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
		return backgroundColor;
	}

	public final void setBackgroundColor(RGB backgroundColor) {
		RGB previousValue = this.backgroundColor;
		this.backgroundColor = backgroundColor;
		getListeners().firePropertyChange(PROPERTY_BACKGROUND_COLOR, previousValue, backgroundColor);
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new TextPropertyDescriptor(PROPERTY_TEXT, "Text"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_TEXT_COLOR, "Text color"));
		properties.add(new ColorPropertyDescriptor(PROPERTY_BACKGROUND_COLOR, "Background color"));
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		if (propertyName.equals(PROPERTY_TEXT))
			return getText();

		if (propertyName.equals(PROPERTY_TEXT_COLOR))
			return getTextColor();

		if (propertyName.equals(PROPERTY_BACKGROUND_COLOR))
			return getBackgroundColor();

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		if (propertyName.equals(PROPERTY_TEXT))
			setText((String) value);

		if (propertyName.equals(PROPERTY_TEXT_COLOR))
			setTextColor((RGB) value);

		if (propertyName.equals(PROPERTY_BACKGROUND_COLOR))
			setBackgroundColor((RGB) value);
	}
}
