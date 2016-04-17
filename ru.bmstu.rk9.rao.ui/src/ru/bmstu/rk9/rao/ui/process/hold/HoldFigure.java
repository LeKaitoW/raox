package ru.bmstu.rk9.rao.ui.process.hold;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.node.BlockFigure;

public class HoldFigure extends BlockFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillRectangle(bounds);

			Rectangle clockFaceBounds = bounds.getCopy();
			final int clockFaceBorder = Math.min(bounds.width, bounds.height) / 7;
			clockFaceBounds.shrink(clockFaceBorder, clockFaceBorder);
			drawClock(graphics, clockFaceBounds, 10, 10);
		}

		private static IFigure create() {
			return new Shape();
		}

		private void drawClock(Graphics graphics, Rectangle bounds, final int hours, final int minutes) {
			graphics.setBackgroundColor(ProcessColors.MODEL_BACKGROUND_COLOR);
			graphics.fillArc(bounds, 0, 360);

			graphics.setBackgroundColor(getForegroundColor());
			graphics.translate(bounds.getCenter());

			final float clockRadius = Math.min(bounds.width, bounds.height) / 2;

			final float timestampWidth = clockRadius * 0.25f;
			final float timestampHeight = timestampWidth * 0.20f;
			for (int timestampIndex = 0; timestampIndex < 4; ++timestampIndex) {
				graphics.fillRectangle((int) (clockRadius - timestampWidth - timestampHeight), (int) (-timestampHeight),
						(int) (timestampWidth), (int) (timestampHeight * 2));
				graphics.rotate(90);
			}

			final float minutes_degrees = (float) minutes / 60 * 360 - 90;
			graphics.rotate(minutes_degrees);
			final int minuteHandOfClockWidth = (int) (clockRadius * 0.85);
			final int minuteHandOfClockHegiht = Math.max(minuteHandOfClockWidth / 20, 1);
			graphics.fillRectangle(0, -minuteHandOfClockHegiht, minuteHandOfClockWidth, minuteHandOfClockHegiht * 2);
			graphics.rotate(-minutes_degrees);

			final float hours_degrees = (float) hours / 12 * 360 - 90;
			graphics.rotate(hours_degrees);
			final int hourHandOfClockWidth = (int) (minuteHandOfClockWidth * 0.8);
			final int hourHandOfClockHegiht = (int) Math.max(minuteHandOfClockHegiht * 1.5, 1);
			graphics.fillRectangle(0, -hourHandOfClockHegiht, hourHandOfClockWidth, hourHandOfClockHegiht * 2);
			graphics.rotate(-hours_degrees);
		}
	}

	public HoldFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(HoldNode.DOCK_IN, inputConnectionAnchor);

		ConnectionAnchor outputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(HoldNode.DOCK_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.setOffsetHorizontal(0);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(bounds.width);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(HoldNode.name);
	}
}
