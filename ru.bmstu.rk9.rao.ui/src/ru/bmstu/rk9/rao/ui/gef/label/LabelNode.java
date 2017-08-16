package ru.bmstu.rk9.rao.ui.gef.label;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.List;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.DefaultColors;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.font.Font;
import ru.bmstu.rk9.rao.ui.gef.font.FontPropertyDescriptor;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;

public class LabelNode extends Node implements Serializable, PropertyChangeListener {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_TEXT = "Text";
	protected static final String PROPERTY_TEXT_COLOR = "Text color";
	protected static final String PROPERTY_BACKGROUND_COLOR = "Background color";
	public static final String PROPERTY_FONT = "Font";
	protected static final String PROPERTY_ALIGNMENT = "Alignment";
	protected static final String PROPERTY_VISIBLE = "Visible";
	public static String name = "Label";
	private final int border = 5;

	private String text = "text";
	private RGB textColor = DefaultColors.LABEL_TEXT_COLOR.getRGB();
	private RGB backgroundColor;
	private Font font;
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
		// Вариант с передачей шрифта от модели не работает, поскольку при
		// изменении глобального шрифта проверяется, совпадал ли данный шрифт с
		// новым глобальным, но в том случае локальный шрифт уже будет
		// измененным, потому надежнее просто запоминать шрифт
		if (font == null)
			font = getDefaultFont();
		return font;
	}

	public final void setFont(Font font) {
		Font previousValue = getFont();
		this.font = font == null ? getDefaultFont() : font;
		getListeners().firePropertyChange(PROPERTY_FONT, previousValue, font);
	}

	private Font getDefaultFont() {
		return ((ModelNode) getRoot()).getFont();
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

	public final Dimension getTextBounds(Font serializableFontData) {
		Dimension dimension = FigureUtilities.getStringExtents(getText(), serializableFontData.getSwtFont());
		dimension.expand(border * 2, border * 2);
		return dimension;
	}

	public final Dimension getTextBounds() {
		return getTextBounds(getFont());
	}

	/**
	 * Все субклассы, переопределяющие данный метод, должны вызывать
	 * <code>super.afterCreate()</code> поскольку в нем происходит подписка к
	 * наблюдателю
	 */
	@Override
	public void assignedToParent() {
		((ModelNode) getRoot()).addPropertyChangeListener(this);
	}

	/**
	 * Все субклассы, переопределяющие данный метод, должны вызывать
	 * <code>super.onDelete()</code> поскольку в нем происходит отписка от
	 * наблюдателя
	 */
	@Override
	public void onDelete() {
		ModelNode modelNode = (ModelNode) getRoot();
		modelNode.removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case ModelNode.PROPERTY_FONT:
			Font oldGlovalFont = (Font) evt.getOldValue();
			Font localFont = getFont();
			if (localFont.equals(oldGlovalFont)) {
				// Наш шрифт совпадал со старым глобальным, так что меняем
				setFont(null);
			}
			break;
		}

	}
}
