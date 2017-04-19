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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.SymbolAxis;
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
	private final ExtendedToolTip toolTip;

	public PlotFrame(final Composite comp, final int style) {
		super(comp, style, null, ChartComposite.DEFAULT_WIDTH, ChartComposite.DEFAULT_HEIGHT, 0, 0, Integer.MAX_VALUE,
				Integer.MAX_VALUE, ChartComposite.DEFAULT_BUFFER_USED, true, true, true, true, true);
		addSWTListener(new PlotKeyListener());
		addMouseWheelListener(new PlotMouseWheelListener());
		addSWTListener(new PlotMouseMoveListener());
		setZoomInFactor(0.75);
		setZoomOutFactor(1.25);
		toolTip = new ExtendedToolTip(this, org.eclipse.swt.SWT.BALLOON, false);
		toolTip.setBackgroundColor(new Color(comp.getDisplay(), 255, 255, 255));
		toolTip.setForegroundColor(new Color(comp.getDisplay(), 128, 128, 128));
		toolTip.setRespectDisplayBounds(false);
		toolTip.setShift(new Point(0, -50));
		toolTip.setHideDelay(0);
		toolTip.setHideOnMouseDown(false);
		toolTip.setPopupDelay(0);
	}

	private Point plotToSwt(double x, double y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		double widthInAxisUnits = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		double heightInAxisUnits = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = (x - domainAxis.getLowerBound()) / widthInAxisUnits;
		double relativeY = (rangeAxis.getUpperBound() - y) / heightInAxisUnits;
		double screenX = relativeX * rectangle.width + rectangle.x;
		double screenY = relativeY * rectangle.height + rectangle.y;

		return new Point((int) Math.round(screenX), (int) Math.round(screenY));
	}

	 private class ExtendedToolTip extends DefaultToolTip {
		private boolean isActive = false;

		ExtendedToolTip(Control control, int style, boolean manualActivation) {
			super(control, style, manualActivation);
		}
	}

	private Point2D swtToPlot(int x, int y) {
		Rectangle rectangle = PlotFrame.this.getScreenDataArea();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		final double widthInAxisUnits = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		final double heightInAxisUnits = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = ((double) x - rectangle.x) / (rectangle.width);
		double relativeY = ((double) y - rectangle.y) / (rectangle.height);
		double xPlot = domainAxis.getLowerBound() + relativeX * widthInAxisUnits;
		double yPlot = rangeAxis.getUpperBound() - relativeY * heightInAxisUnits;

		return new Point2D.Double(xPlot, yPlot);
	}

	private class PlotMouseMoveListener implements MouseMoveListener {
		final static int MAX_HINT_DISTANCE = 50;
		int previousIndex = -1;
		int distanceToMouse = 0;
		Point widgetPoint = null;
		double valueX;
		double valueY;
		final static int BORDER = 1;

		private int getDistance(Point x1, Point x2) {
			return (int) Math.sqrt(Math.pow((x1.x - x2.x), 2) + Math.pow((x1.y - x2.y), 2));
		}

		@Override
		public void mouseMove(MouseEvent e) {
			Point mousePoint = new Point(Math.round(e.x), Math.round(e.y));
			XYDataset dataset = getChart().getXYPlot().getDataset();
			int itemsCount = dataset.getItemCount(0);
			java.awt.Color backgroundColor = (java.awt.Color) getChart().getXYPlot().getBackgroundPaint();

			if (itemsCount > 0) {
				double previousDistance = Double.MAX_VALUE;
				int currentIndex = -1;

				for (int index = 0; index < itemsCount; index++) {
					valueX = dataset.getXValue(0, index);
					valueY = dataset.getYValue(0, index);
					widgetPoint = plotToSwt(valueX, valueY);
					distanceToMouse = getDistance(widgetPoint, mousePoint);

					if (distanceToMouse < previousDistance) {
						previousDistance = distanceToMouse;
						currentIndex = index;
					}
				}

				mousePoint = new Point(Math.round(e.x), Math.round(e.y));
				valueX = dataset.getXValue(0, currentIndex);
				valueY = dataset.getYValue(0, currentIndex);
				widgetPoint = plotToSwt(valueX, valueY);
				distanceToMouse = getDistance(widgetPoint, mousePoint);
				Rectangle screenDataArea = getScreenDataArea();
				Rectangle realAreaOfPlot = new Rectangle(screenDataArea.x - BORDER, screenDataArea.y - BORDER,
						screenDataArea.width + 2 * BORDER, screenDataArea.height + 2 * BORDER);

				if (currentIndex >= 0 && realAreaOfPlot.contains(mousePoint) && distanceToMouse < MAX_HINT_DISTANCE
						&& (toolTip.isActive == false || previousIndex != currentIndex)) {
					getChart().getXYPlot().clearAnnotations();
					toolTip.isActive = true;
					previousIndex = currentIndex;
					Rectangle rectangle = PlotFrame.this.getScreenDataArea();
					final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
					final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
					double widthInAxisUnits = domainAxis.getUpperBound() - domainAxis.getLowerBound();
					double heightInAxisUnits = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
					double radiusOfCircleX = 5 * widthInAxisUnits / rectangle.width;
					double radiusOfCircleY = 5 * heightInAxisUnits / rectangle.height;

					Ellipse2D Circle = new Ellipse2D.Float((float) (valueX - radiusOfCircleX),
							(float) (valueY - radiusOfCircleY), (float) (2 * radiusOfCircleX),
							(float) (2 * radiusOfCircleY));
					Paint seriesColor = getChart().getXYPlot().getRenderer().getSeriesPaint(0);
					XYShapeAnnotation annotation = new XYShapeAnnotation(Circle, new BasicStroke(3.0f), seriesColor,
							backgroundColor);
					getChart().getXYPlot().addAnnotation(annotation, false);
					final String tooltipText;

					if (rangeAxis instanceof SymbolAxis) {
						int valueYId = (int) Math.round(valueY);
						final SymbolAxis symbolRangeAxis = (SymbolAxis) rangeAxis;
						tooltipText = String.format("%s%n%.3f", symbolRangeAxis.getSymbols()[valueYId], valueX);
					} else {
						tooltipText = String.format("%.3f%n%.3f", valueY, valueX);
					}

					toolTip.setText(tooltipText);
					toolTip.show(widgetPoint);
				}

				if ((distanceToMouse > MAX_HINT_DISTANCE || !realAreaOfPlot.contains(mousePoint))
						&& toolTip.isActive == true) {
					toolTip.hide();
					toolTip.isActive = false;
					getChart().getXYPlot().clearAnnotations();
				}
			}
		}
	}

	private class PlotKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.keyCode) {
			case SWT.SHIFT:
				setRangeZoomable(true);
				return;
			case SWT.CTRL:
				setDomainZoomable(true);
				return;
			case SWT.SPACE:
				restoreAutoBounds();
				return;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == SWT.SHIFT)
				setRangeZoomable(false);
			else if (e.keyCode == SWT.CTRL)
				setDomainZoomable(false);
		}
	}

	private class PlotMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseScrolled(MouseEvent e) {
			if (e.count > 0 && isRangeZoomable())
				zoomInRange(e.x, e.y);
			if (e.count < 0 && isRangeZoomable())
				zoomOutRange(e.x, e.y);
			if (e.count > 0 && isDomainZoomable())
				zoomInDomain(e.x, e.y);
			if (e.count < 0 && isDomainZoomable())
				zoomOutDomain(e.x, e.y);
			if (e.count > 0 && isDomainZoomable() && isRangeZoomable())
				zoomInBoth(e.x, e.y);
			if (e.count < 0 && isDomainZoomable() && isRangeZoomable())
				zoomOutBoth(e.x, e.y);

			if (!isDomainZoomable() && !isRangeZoomable()
					&& horizontalSlider.isVisible()
					&& horizontalSlider.getThumb() < 100) {
				horizontalSlider.setSelection(horizontalSlider.getSelection() - e.count);
				horizontalSelected();
			}

		}
	}

	private void horizontalSelected() {
		getChart().getXYPlot().getDomainAxis().setLowerBound(horizontalSlider.getSelection() / horizontalRatio);
		getChart().getXYPlot().getDomainAxis().setUpperBound(
				(horizontalSlider.getThumb() + horizontalSlider.getSelection()) / horizontalRatio);
	}

	private void verticalSelected() {
		getChart().getXYPlot().getRangeAxis().setLowerBound(
				(verticalSlider.getMaximum() - verticalSlider.getSelection() - verticalSlider.getThumb())
						/ verticalRatio);
		getChart().getXYPlot().getRangeAxis()
				.setUpperBound((verticalSlider.getMaximum() - verticalSlider.getSelection()) / verticalRatio);
	}

	public final void setSliders(final Slider horizontalSlider, final Slider verticalSlider) {
		this.horizontalSlider = horizontalSlider;
		this.verticalSlider = verticalSlider;
		horizontalSlider.setMinimum(0);
		verticalSlider.setMinimum(0);
		this.horizontalSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				horizontalSelected();
			}
		});

		this.verticalSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verticalSelected();
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
		getChart().getXYPlot().clearAnnotations();
	}

	@Override
	public void zoomInRange(double x, double y) {
		super.zoomInRange(x, y);
		updateSliders();
		getChart().getXYPlot().clearAnnotations();
	}

	@Override
	public void zoomOutDomain(double x, double y) {
		super.zoomOutDomain(x, y);
		updateSliders();
		getChart().getXYPlot().clearAnnotations();
	}

	@Override
	public void zoomOutRange(double x, double y) {
		super.zoomOutRange(x, y);
		updateSliders();
		getChart().getXYPlot().clearAnnotations();
	}

	public final void updateSliders() {
		getChart().getXYPlot().clearAnnotations();
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		final double SLIDER_EPSILON = 0.001;

		if (Math.abs(horizontalMaximum - domainAxis.getRange().getUpperBound()) > SLIDER_EPSILON) {
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

		if (Math.abs(verticalMaximum - rangeAxis.getRange().getUpperBound()) > SLIDER_EPSILON) {
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
