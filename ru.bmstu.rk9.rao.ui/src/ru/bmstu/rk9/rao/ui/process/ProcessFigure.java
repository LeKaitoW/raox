package ru.bmstu.rk9.rao.ui.process;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

public class ProcessFigure extends Figure {

	protected Label label = new Label();
	private boolean isNameVisible = true;
	protected Hashtable connectionAnchors = new Hashtable(7);
	protected Vector inputConnectionAnchors = new Vector(2, 2);
	protected Vector outputConnectionAnchors = new Vector(2, 2);

	public ProcessFigure() {
		ProcessConnectionAnchor inputConnectionAnchor, outputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = 4;
		inputConnectionAnchors.addElement(inputConnectionAnchor);
		connectionAnchors.put(Node.TERMINAL_IN, inputConnectionAnchor);

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchor.offsetHorizontal = 10;
		outputConnectionAnchors.addElement(outputConnectionAnchor);
		connectionAnchors.put(Node.TERMINAL_OUT, outputConnectionAnchor);

		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		Font oldFont = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry()
				.get(PreferenceConstants.EDITOR_TEXT_FONT);
		FontData[] fontData = oldFont.getFontData();
		fontData[0].setHeight(6);
		font = new Font(oldFont.getDevice(), fontData);
		add(label);
		setOpaque(true);
	}

	private Font font;

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}

	@Override
	public Font getFont() {
		return font;
	}

	public void setFigureNameVisible(boolean visible) {
		isNameVisible = visible;
		paintName(this.getBounds());
	}

	protected void paintName(Rectangle rectangle) {
		Rectangle relativeRectangle = rectangle.getCopy();
		relativeRectangle.setX(0);
		relativeRectangle.setY(rectangle.height * 2 / 5);

		label.setFont(getFont());
		add(label);
		setConstraint(label, relativeRectangle);
		label.setVisible(isNameVisible);
	}

	public ConnectionAnchor getConnectionAnchor(String terminal) {
		return (ConnectionAnchor) connectionAnchors.get(terminal);
	}

	public String getConnectionAnchorName(ConnectionAnchor c) {
		Enumeration keys = connectionAnchors.keys();
		String key;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			if (connectionAnchors.get(key).equals(c))
				return key;
		}
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration e = getSourceConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector getSourceConnectionAnchors() {
		return outputConnectionAnchors;
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		ConnectionAnchor closest = null;
		long min = Long.MAX_VALUE;

		Enumeration e = getTargetConnectionAnchors().elements();
		while (e.hasMoreElements()) {
			ConnectionAnchor c = (ConnectionAnchor) e.nextElement();
			Point p2 = c.getLocation(null);
			long d = p.getDistance2(p2);
			if (d < min) {
				min = d;
				closest = c;
			}
		}
		return closest;
	}

	public Vector getTargetConnectionAnchors() {
		return inputConnectionAnchors;
	}
}
