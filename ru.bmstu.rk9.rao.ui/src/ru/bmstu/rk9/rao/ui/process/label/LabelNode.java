package ru.bmstu.rk9.rao.ui.process.label;

import java.io.Serializable;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class LabelNode extends Node implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_TEXT_COLOR = "Text color";
	public static final String PROPERTY_BACKGROUND_COLOR = "Background color";
	public static String name = "Label";

	private RGB textColor;
	private RGB backgroundColor;

	public LabelNode() {
		textColor = ProcessColors.LABEL_TEXT_COLOR.getRGB();
		backgroundColor = ProcessColors.LABEL_BACKGROUND_COLOR.getRGB();
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
}
