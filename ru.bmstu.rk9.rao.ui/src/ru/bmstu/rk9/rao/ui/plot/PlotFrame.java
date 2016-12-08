package ru.bmstu.rk9.rao.ui.plot;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

public class PlotFrame extends ChartComposite {

	private Slider horizontalSlider;
	private Slider verticalSlider;
	private double horizontalMaximum;
	private double verticalMaximum;
	private double horizontalRatio;
	private double verticalRatio;

	private final Point2D swtToPlot(int x, int y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		final double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		final double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = ((double) x - rectangle.x) / (rectangle.width);
		double relativeY = ((double) y - rectangle.y) / (rectangle.height);
		double x_plot = domainAxis.getLowerBound() + relativeX * width_in_axis_units;
		double y_plot = rangeAxis.getUpperBound() - relativeY * height_in_axis_units;

		return new Point2D.Double(x_plot, y_plot);
	}

	private final Point plotToSwt(double x, double y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = (x - domainAxis.getLowerBound()) / width_in_axis_units;
		double relativeY = (rangeAxis.getUpperBound() - y) / height_in_axis_units;
		double screenX = relativeX * rectangle.width + rectangle.x;
		double screenY = relativeY * rectangle.height + rectangle.y;

		return new Point((int) Math.round(screenX), (int) Math.round(screenY));
	}

	final DefaultToolTip toolTip;

	class PlotMouseMoveListener implements MouseMoveListener {

		int previousIndex = -1;
		volatile boolean tooltipWasShown = false;
		int distanceToMouse = 0;
		Point widgetPoint = null;
		double valueX;
		double valueY;

		final private int distance(Point x1, Point x2) {
			return (int) Math.sqrt(Math.pow((x1.x - x2.x), 2) + Math.pow((x1.y - x2.y), 2));
		}

		@Override
		public void mouseMove(MouseEvent e) {
			Point mousePoint = new Point(Math.round(e.x), Math.round(e.y));
			XYDataset dataset = getChart().getXYPlot().getDataset();

			Paint bckg = getChart().getXYPlot().getBackgroundPaint();
			java.awt.Color bckgColor = java.awt.Color.BLACK;
			if (bckg instanceof java.awt.Color) {
				bckgColor = (java.awt.Color) bckg;
			}

			int itemsCount = dataset.getItemCount(0);
			if (itemsCount > 0) {
				double previousDistance = Double.MAX_VALUE;
				int currentIndex = -1;
				for (int index = 0; index < itemsCount; index++) {
					valueX = dataset.getXValue(0, index);
					valueY = dataset.getYValue(0, index);
					widgetPoint = plotToSwt(valueX, valueY);
					distanceToMouse = distance(widgetPoint, mousePoint);
					if (distanceToMouse < previousDistance) {
						previousDistance = distanceToMouse;
						currentIndex = index;
					}
				}
				mousePoint = new Point(Math.round(e.x), Math.round(e.y));
				valueX = dataset.getXValue(0, currentIndex);
				valueY = dataset.getYValue(0, currentIndex);
				widgetPoint = plotToSwt(valueX, valueY);
				distanceToMouse = distance(widgetPoint, mousePoint);

				if (currentIndex >= 0 && (currentIndex != previousIndex || !tooltipWasShown)) {
					getChart().getXYPlot().clearAnnotations();
					System.out.println(mousePoint + " " + e.x);
					previousIndex = currentIndex;
					System.out.println(distanceToMouse + " " + widgetPoint + " " + mousePoint);
					Rectangle rectangle = PlotFrame.this.getScreenDataArea();
					final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
					final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
					double width_in_axis_units = domainAxis.getUpperBound() - domainAxis.getLowerBound();
					double height_in_axis_units = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
					double radiusOfCircleX = 5 * width_in_axis_units / rectangle.width;
					double radiusOfCircleY = 5 * height_in_axis_units / rectangle.height;
					Ellipse2D Circle = new Ellipse2D.Float((float) (valueX - radiusOfCircleX),
							(float) (valueY - radiusOfCircleY), (float) (2 * radiusOfCircleX),
							(float) (2 * radiusOfCircleY));
					Paint seriesPaint = getChart().getXYPlot().getRenderer().getSeriesPaint(0);

					java.awt.Color seriesColor = java.awt.Color.GREEN;
					if (seriesPaint instanceof java.awt.Color) {
						seriesColor = (java.awt.Color) seriesPaint;
					}

					XYShapeAnnotation annotation = new XYShapeAnnotation(Circle, new BasicStroke(3.0f), seriesColor,
							bckgColor);
					getChart().getXYPlot().addAnnotation(annotation, false);
					Rectangle screenDataArea = getScreenDataArea();

					if (new Rectangle(screenDataArea.x - 1, screenDataArea.y - 1, screenDataArea.width + 2,
							screenDataArea.height + 2).contains(mousePoint) && distanceToMouse < 50) {
						toolTip.setText(String.format("%.3f%n%.3f", valueY, valueX));
						toolTip.show(widgetPoint);
						tooltipWasShown = true;

					} else {
						toolTip.hide();
						tooltipWasShown = false;
						getChart().getXYPlot().clearAnnotations();
					}
				}
			}

		}
	}

	class PlotKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.SHIFT: {
				setRangeZoomable(true);
				return;
			}
			case SWT.CTRL: {
				setDomainZoomable(true);
				return;
			}
			case SWT.SPACE:
				restoreAutoBounds();
				return;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.SHIFT) {
				setRangeZoomable(false);
			} else if (e.keyCode == SWT.CTRL) {
				setDomainZoomable(false);
			}
		}
	}

	class PlotMouseWheelListener implements MouseWheelListener {

		@Override
		public void mouseScrolled(MouseEvent e) {

			if (e.count > 0 && isRangeZoomable()) {
				zoomInRange(e.x, e.y);
			}
			if (e.count < 0 && isRangeZoomable()) {
				zoomOutRange(e.x, e.y);
			}
			if (e.count > 0 && isDomainZoomable()) {
				zoomInDomain(e.x, e.y);
			}
			if (e.count < 0 && isDomainZoomable()) {
				zoomOutDomain(e.x, e.y);
			}
			if (e.count > 0 && isDomainZoomable() && isRangeZoomable()) {
				zoomInBoth(e.x, e.y);
			}
			if (e.count < 0 && isDomainZoomable() && isRangeZoomable()) {
				zoomOutBoth(e.x, e.y);
			}
		}
	}

	public PlotFrame(final Composite comp, final int style) {
		super(comp, style, null, ChartComposite.DEFAULT_WIDTH, ChartComposite.DEFAULT_HEIGHT, 0, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE, ChartComposite.DEFAULT_BUFFER_USED, true, true, true, true, true);
		addSWTListener(new PlotKeyListener());
		addMouseWheelListener(new PlotMouseWheelListener());
		addSWTListener(new PlotMouseMoveListener());
		setZoomInFactor(0.75);
		setZoomOutFactor(1.25);
		toolTip = new DefaultToolTip(this, SWT.BALLOON, false);
		toolTip.setBackgroundColor(new Color(comp.getDisplay(), 255, 255, 255));
		toolTip.setForegroundColor(new Color(comp.getDisplay(), 128, 128, 128));
		toolTip.setRespectDisplayBounds(true);
		toolTip.setShift(new Point(0, -50));
		toolTip.setHideDelay(0);
		toolTip.setHideOnMouseDown(false);
		toolTip.setPopupDelay(0);

	}

	public final void setSliders(final Slider horizontalSlider, final Slider verticalSlider) {
		this.horizontalSlider = horizontalSlider;
		this.verticalSlider = verticalSlider;
		horizontalSlider.setMinimum(0);
		verticalSlider.setMinimum(0);
		this.horizontalSlider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getChart().getXYPlot().getDomainAxis().setLowerBound(horizontalSlider.getSelection() / horizontalRatio);
				getChart().getXYPlot().getDomainAxis().setUpperBound(
						(horizontalSlider.getThumb() + horizontalSlider.getSelection()) / horizontalRatio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		this.verticalSlider.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getChart().getXYPlot().getRangeAxis().setLowerBound(
						(verticalSlider.getMaximum() - verticalSlider.getSelection() - verticalSlider.getThumb())
								/ verticalRatio);
				getChart().getXYPlot().getRangeAxis()
						.setUpperBound((verticalSlider.getMaximum() - verticalSlider.getSelection()) / verticalRatio);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public final void setChartMaximum(final double horizontalMaximum, final double verticalMaximum) {
		final double freeSpaceCoefficient = 1.05;
		this.horizontalMaximum = horizontalMaximum * freeSpaceCoefficient;
		this.verticalMaximum = verticalMaximum * freeSpaceCoefficient;
	}

	@Override
	public void zoomInDomain(double x, double y) {
		super.zoomInDomain(x, y);
		updateSliders();
	}

	@Override
	public void zoomInRange(double x, double y) {
		super.zoomInRange(x, y);
		updateSliders();
	}

	@Override
	public void zoomOutDomain(double x, double y) {
		super.zoomOutDomain(x, y);
		updateSliders();
	}

	@Override
	public void zoomOutRange(double x, double y) {
		super.zoomOutRange(x, y);
		updateSliders();
	}

	public final void updateSliders() {
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		final double sliderConst = 0.001;
		if (horizontalMaximum - domainAxis.getRange().getUpperBound() > sliderConst) {
			horizontalSlider.setVisible(true);
			horizontalSlider.setEnabled(true);
			horizontalRatio = horizontalSlider.getMaximum() / horizontalMaximum;
			horizontalSlider.setThumb(
					(int) Math.round((domainAxis.getUpperBound() - domainAxis.getLowerBound()) * horizontalRatio));
			horizontalSlider.setSelection((int) Math.round(domainAxis.getLowerBound() * horizontalRatio));
		} else {
			horizontalSlider.setVisible(false);
			horizontalSlider.setEnabled(false);

		}
		if (verticalMaximum - rangeAxis.getRange().getUpperBound() > sliderConst) {
			verticalSlider.setVisible(true);
			verticalSlider.setEnabled(true);

			verticalRatio = verticalSlider.getMaximum() / verticalMaximum;
			verticalSlider.setThumb(
					(int) Math.round((rangeAxis.getUpperBound() - rangeAxis.getLowerBound()) * verticalRatio));
			verticalSlider
					.setSelection((int) Math.round((verticalMaximum - rangeAxis.getUpperBound()) * verticalRatio));
		} else {
			verticalSlider.setVisible(false);
			verticalSlider.setEnabled(false);
		}
	}

	@Override
	public void zoom(Rectangle selection) {
		super.zoom(selection);
		horizontalSlider.setVisible(true);
		horizontalSlider.setEnabled(true);
		if (this.isRangeZoomable()) {
			verticalSlider.setVisible(true);
			verticalSlider.setEnabled(true);
		}
		updateSliders();
	}

	@Override
	public void restoreAutoBounds() {
		super.restoreAutoBounds();
		horizontalSlider.setEnabled(false);
		horizontalSlider.setVisible(false);
		verticalSlider.setEnabled(false);
		verticalSlider.setVisible(false);
	}
}
