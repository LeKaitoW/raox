package ru.bmstu.rk9.rao.ui.process.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.INodeFigure;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionAnchor;

public class BlockFigure extends Figure implements INodeFigure {

	protected Map<String, ConnectionAnchor> connectionAnchors = new HashMap<>();
	protected List<ConnectionAnchor> inputConnectionAnchors = new ArrayList<>();
	protected List<ConnectionAnchor> outputConnectionAnchors = new ArrayList<>();

	private static final int shapeBorder = 5;
	private IFigure shape;
	private Docks docks = new Docks();
	public static final int dockSize = 4;
	private static final Rectangle dockRectangle = new Rectangle();
	@SuppressWarnings("serial")
	private static final List<Point> translatePoints = new ArrayList<Point>() {
		{
			add(new Point(1, 0));
			add(new Point(0, 1));
			add(new Point(-1, 0));
			add(new Point(-1, 0));
			add(new Point(0, -1));
			add(new Point(0, -1));
			add(new Point(1, 0));
			add(new Point(1, 0));
		}
	};

	class Docks extends Figure {
		@Override
		protected final void paintFigure(Graphics graphics) {
			Rectangle bounds = getShape().getBounds();
			for (Entry<String, ConnectionAnchor> entry : connectionAnchors.entrySet()) {
				final int dockCenterX = bounds.x + entry.getValue().getOffsetHorizontal();
				final int dockCenterY = bounds.y + entry.getValue().getOffsetVertical();
				drawDock(graphics, dockCenterX, dockCenterY);
			}
		}

		private final void drawDock(Graphics graphics, final int dockCenterX, final int dockCenterY) {
			dockRectangle.x = dockCenterX - dockSize;
			dockRectangle.y = dockCenterY - dockSize;
			dockRectangle.width = dockSize * 2;
			dockRectangle.height = dockSize * 2;

			graphics.setBackgroundColor(getParent().getBackgroundColor());
			graphics.fillRectangle(dockRectangle);

			dockRectangle.shrink(1, 1);
			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillRectangle(dockRectangle);
		}

	}

	public BlockFigure(IFigure shape) {
		this.shape = shape;

		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		setOpaque(false);

		add(this.shape);
		add(docks);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Rectangle shapeBounds = figure.getBounds().getCopy();
				shapeBounds.x = shapeBorder;
				shapeBounds.y = shapeBorder;
				shapeBounds.width -= shapeBounds.x + shapeBorder;
				shapeBounds.height -= shapeBounds.y + shapeBorder;
				setConstraint(getShape(), shapeBounds);

				Rectangle docksBounds = figure.getBounds().getCopy();
				docksBounds.x = 0;
				docksBounds.y = 0;
				docksBounds.height -= docksBounds.y;
				setConstraint(docks, docksBounds);
			}
		});
	}

	@Override
	protected final void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);

		Color shapeColor = shape.getForegroundColor();

		shape.setForegroundColor(shape.getBackgroundColor());
		for (Point translatePoint : translatePoints) {
			shape.translate(translatePoint.x, translatePoint.y);
			shape.paint(graphics);
		}

		shape.translate(-1, 1);
		shape.setForegroundColor(shapeColor);
	}

	public void setConstraint(Rectangle constraint) {
		getParent().setConstraint(this, constraint);
	}

	protected final IFigure getShape() {
		return shape;
	}

	public ConnectionAnchor getConnectionAnchor(String dockName) {
		return connectionAnchors.get(dockName);
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

	@Override
	public void assignSettings(IFigure original) {
		setForegroundColor(original.getForegroundColor());
		setBackgroundColor(original.getBackgroundColor());
	}
}
