package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

public class ProcessFigure extends Figure {

	protected Label label = new Label();
	private boolean isNameVisible = true;
	protected Map<String, ProcessConnectionAnchor> connectionAnchors = new HashMap<>();
	protected List<ConnectionAnchor> inputConnectionAnchors = new ArrayList<>();
	protected List<ConnectionAnchor> outputConnectionAnchors = new ArrayList<>();
	protected static final int offset = 5;
	protected static Color pageBackgroundColor = ColorConstants.white;

	public ProcessFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		Font oldFont = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry()
				.get(PreferenceConstants.EDITOR_TEXT_FONT);
		FontData[] fontData = oldFont.getFontData();
		fontData[0].setHeight(6);
		font = new Font(oldFont.getDevice(), fontData);
		add(label);
		label.setFont(getFont());
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

	@Override
	final protected void paintFigure(Graphics graphics) {
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.setTextAntialias(SWT.ON);

		drawShape(graphics);
		drawDocks(graphics);
		drawName(graphics);
	}

	protected void drawShape(Graphics graphics) {
	}

	private final void drawDocks(Graphics graphics) {
		Rectangle bounds = getBounds();
		for (Entry<String, ProcessConnectionAnchor> entry : connectionAnchors.entrySet()) {
			final int dockCenterX = bounds.x + entry.getValue().offsetHorizontal + 1;
			final int dockCenterY = bounds.y + entry.getValue().offsetVertical + 1;
			drawDock(graphics, dockCenterX, dockCenterY);
		}
	}

	private static final Rectangle dockRectangle = new Rectangle();
	protected static final int dockSize = 4;

	private final void drawDock(Graphics graphics, final int dockCenterX, final int dockCenterY) {
		dockRectangle.x = dockCenterX - dockSize;
		dockRectangle.y = dockCenterY - dockSize;
		dockRectangle.width = dockSize * 2;
		dockRectangle.height = dockSize * 2;
		graphics.setBackgroundColor(pageBackgroundColor);
		graphics.fillRectangle(dockRectangle);

		dockRectangle.shrink(1, 1);
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(dockRectangle);
	}

	public void setFigureNameVisible(boolean visible) {
		isNameVisible = visible;
		invalidate();
	}

	private final void drawName(Graphics graphics) {
		Rectangle relativeRectangle = getBounds().getCopy();
		relativeRectangle.setX(0);
		relativeRectangle.setHeight(10);
		relativeRectangle.setY(0);
		setConstraint(label, relativeRectangle);
		label.setVisible(isNameVisible);
	}

	public ConnectionAnchor getConnectionAnchor(String terminal) {
		return connectionAnchors.get(terminal);
	}

	public String getConnectionAnchorName(ConnectionAnchor connectionAnchor) {
		for (Entry<String, ProcessConnectionAnchor> entry : connectionAnchors.entrySet()) {
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

	public List<ConnectionAnchor> getSourceConnectionAnchors() {
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

	public List<ConnectionAnchor> getTargetConnectionAnchors() {
		return inputConnectionAnchors;
	}
}
