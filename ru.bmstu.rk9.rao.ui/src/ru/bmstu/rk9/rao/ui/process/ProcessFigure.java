package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
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
	protected HashMap<String, ConnectionAnchor> connectionAnchors = new HashMap<>();
	protected ArrayList<ConnectionAnchor> inputConnectionAnchors = new ArrayList<>();
	protected ArrayList<ConnectionAnchor> outputConnectionAnchors = new ArrayList<>();

	public ProcessFigure() {
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
		return connectionAnchors.get(terminal);
	}

	public String getConnectionAnchorName(ConnectionAnchor connectionAnchor) {
		for (Entry<String, ConnectionAnchor> entry : connectionAnchors.entrySet()) {
			if (entry.getValue().equals(connectionAnchor)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchorAt(Point point) {
		ConnectionAnchor closest = null;
		double min = Double.MAX_VALUE;

		for (ConnectionAnchor connectionAnchor : getSourceConnectionAnchors()) {
			Point locationPoint = connectionAnchor.getLocation(null);
			double distance = point.getDistance(locationPoint);
			if (distance < min) {
				min = distance;
				closest = connectionAnchor;
			}
		}
		return closest;
	}

	public ArrayList<ConnectionAnchor> getSourceConnectionAnchors() {
		return outputConnectionAnchors;
	}

	public ConnectionAnchor getTargetConnectionAnchorAt(Point point) {
		ConnectionAnchor closest = null;
		double min = Double.MAX_VALUE;

		for (ConnectionAnchor connectionAnchor : getTargetConnectionAnchors()) {
			Point locationPoint = connectionAnchor.getLocation(null);
			double distance = point.getDistance(locationPoint);
			if (distance < min) {
				min = distance;
				closest = connectionAnchor;
			}
		}
		return closest;
	}

	public ArrayList<ConnectionAnchor> getTargetConnectionAnchors() {
		return inputConnectionAnchors;
	}
}
