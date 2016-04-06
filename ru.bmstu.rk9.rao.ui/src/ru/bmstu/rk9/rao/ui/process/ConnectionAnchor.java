package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;

public class ConnectionAnchor extends AbstractConnectionAnchor {

	private int offsetHorizontal;
	private int offsetVertical;

	public ConnectionAnchor(IFigure owner) {
		super(owner);
	}

	protected final int getOffsetHorizontal() {
		return offsetHorizontal;
	}

	public final void setOffsetHorizontal(final int offsetHorizontal) {
		Rectangle shapeBounds = getOwner().getBounds();
		final int left = shapeBounds.x + offsetHorizontal - ProcessFigure.dockSize;
		final int right = left + ProcessFigure.dockSize * 2;

		Rectangle figureBounds = getOwner().getParent().getBounds();
		final int overLeft = Math.max(figureBounds.x - left, 0);
		final int overRight = Math.max(right - figureBounds.right(), 0);

		this.offsetHorizontal = offsetHorizontal + overLeft - overRight;
	}

	protected final int getOffsetVertical() {
		return offsetVertical;
	}

	public final void setOffsetVertical(final int offsetVertical) {
		Rectangle shapeBounds = getOwner().getBounds();
		final int top = shapeBounds.y + offsetVertical - ProcessFigure.dockSize;
		final int bottom = top + ProcessFigure.dockSize * 2;

		Rectangle figureBounds = getOwner().getParent().getBounds();
		final int overTop = Math.max(figureBounds.y - top, 0);
		final int overBottom = Math.max(bottom - figureBounds.bottom(), 0);

		this.offsetVertical = offsetVertical + overTop - overBottom;
	}

	@Override
	public void ancestorMoved(IFigure figure) {
		if (figure instanceof ScalableFigure)
			return;
		super.ancestorMoved(figure);
	}

	@Override
	public Point getLocation(Point reference) {
		IFigure figure = getOwner();
		Rectangle bounds = figure.getBounds();
		final int x = bounds.x + offsetHorizontal - 1;
		final int y = bounds.y + offsetVertical - 1;

		Point point = new PrecisionPoint(x, y);
		figure.translateToAbsolute(point);
		return point;
	}

	@Override
	public Point getReferencePoint() {
		return getLocation(null);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ConnectionAnchor) {
			ConnectionAnchor processAnchor = (ConnectionAnchor) object;

			if (offsetHorizontal == processAnchor.offsetHorizontal && offsetVertical == processAnchor.offsetVertical
					&& getOwner() == processAnchor.getOwner()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (offsetHorizontal * 43 + offsetVertical * 47) ^ getOwner().hashCode();
	}
}
