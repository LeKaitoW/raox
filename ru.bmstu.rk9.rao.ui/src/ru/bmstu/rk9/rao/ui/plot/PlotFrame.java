package ru.bmstu.rk9.rao.ui.plot;

import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.ToolTip;
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

	@Override
	public void setDomainZoomable(boolean zoomable) {
		super.setDomainZoomable(zoomable);
		if (horizontalSlider != null) {
			updateSliders();
		}
	}

	@Override
	public void setRangeZoomable(boolean zoomable) {
		super.setRangeZoomable(zoomable);
		if (verticalSlider != null) {
			updateSliders();
		}
	}

	Point2D swtToPlot(int x, int y) {
		Rectangle sdArea = PlotFrame.this.getScreenDataArea();

		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		double xSize = domainAxis.getUpperBound() - domainAxis.getLowerBound();//в единицах оси
		double ySize = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = ((double) x - sdArea.x) / (sdArea.width);//перенос под сис.коорд графика 
		double relativeY = ((double) y - sdArea.y) / (sdArea.height);
		double realX = domainAxis.getLowerBound() + relativeX * xSize;
		double realY = rangeAxis.getUpperBound() - relativeY * ySize;
	
		return new Point2D.Double(realX, realY);

	}

	Point plotToSwt(double x, double y) {
		Rectangle sdArea = PlotFrame.this.getScreenDataArea();

		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();
		double xSize = domainAxis.getUpperBound() - domainAxis.getLowerBound();
		double ySize = rangeAxis.getUpperBound() - rangeAxis.getLowerBound();
		double relativeX = (x - domainAxis.getLowerBound()) / xSize;
		double relativeY = (rangeAxis.getUpperBound() - y) / ySize;
		double screenX = relativeX * sdArea.width + sdArea.x;
		double screenY = relativeY * sdArea.height + sdArea.y;
		return new Point((int) Math.round(screenX), (int) Math.round(screenY));
	}

	final ToolTip toolTip;//

	class PlotMouseMoveListener implements MouseMoveListener {

		int oldBestI = -1;

		@Override
		public void mouseMove(MouseEvent e) {
			System.out.println("x: " + e.x + " y:" + e.y);

			XYDataset dataset = getChart().getXYPlot().getDataset();//сбор информации о точках граф.
			int itemsCount = dataset.getItemCount(0);// число элементов
			if (itemsCount > 0) {
				Point2D plotCoords = swtToPlot(e.x, e.y);// координаты мышки в плотe
				double bestDistance = Double.MAX_VALUE;
				int bestI = -1;
				for (int i = 0; i < itemsCount; i++) {
					double curItemX = dataset.getXValue(0, i);
					double curItemY = dataset.getYValue(0, i);

					double curDistance = plotCoords.distance(curItemX, curItemY);
					if (curDistance < bestDistance) {
						bestDistance = curDistance;
						bestI = i;
					}

				}
				/*
				 * public void mouseMove(MouseEvent e) { System.out.println(
				 * "x: " + e.x + " y:" + e.y);
				 *
				 * XYDataset dataset = getChart().getXYPlot().getDataset(); int
				 * itemsCount = dataset.getItemCount(0); if (itemsCount > 0) {
				 * Point2D plotCoords = swtToPlot(e.x, e.y); double bestDistance
				 * = Double.MAX_VALUE; int bestI = -1; for (int i = 0; i <
				 * itemsCount; i++) { double curItemX = dataset.getXValue(0, i);
				 * double curItemY = dataset.getYValue(0, i);
				 *
				 * double curDistance = plotCoords.distance(curItemX, curItemY);
				 * if (curDistance < bestDistance) { bestDistance = curDistance;
				 * bestI = i; }
				 */
				if (bestI >= 0 && (bestI != oldBestI)) {
					oldBestI = bestI;
					double valueX = dataset.getXValue(0, bestI), valueY = dataset.getYValue(0, bestI);
					Point widgetPoint = plotToSwt(valueX, valueY);
					Rectangle screenDataArea = getScreenDataArea();
					if (new Rectangle(screenDataArea.x - 1, screenDataArea.y - 1, screenDataArea.width + 2,
							screenDataArea.height + 2).contains(widgetPoint)) {
						System.out.println("x: " + e.x + " y:" + e.y + " realX: " + plotCoords.getX() + " realY: "
								+ plotCoords.getY() + " pointX : " + widgetPoint.x + " pointY: " + widgetPoint.y
								+ " nearestX : " + dataset.getXValue(0, bestI) + " nearestY : "
								+ dataset.getYValue(0, bestI)

						);
						Point displayPoint = toDisplay(widgetPoint);
						toolTip.setLocation(displayPoint);
						toolTip.setText("x: " + valueX + " y: " + valueY);

						toolTip.setVisible(true);

					} else {
						toolTip.setVisible(false);
					}

				}

			}

			// TODO Auto-generated method stub

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
			System.out.println("" + getScaleY());
			System.out.println("" + getScaleX());

			if (e.count > 0 && isRangeZoomable() && getScaleY() >= 0.5) {
				zoomInRange(e.x, e.y);
			}
			if (e.count < 0 && isRangeZoomable() && getScaleY() <= 5) {
				zoomOutRange(e.x, e.y);
			}
			if (e.count > 0 && isDomainZoomable() && getScaleX() >= 0.5) {
				zoomInDomain(e.x, e.y);
			}
			if (e.count < 0 && isDomainZoomable() && getScaleX() <= 5) {
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
		setDomainZoomable(false);
		setRangeZoomable(false);
		toolTip = new ToolTip(comp.getShell(), SWT.BALLOON);
		toolTip.setVisible(false);

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

	public final void updateSliders() {
		final ValueAxis domainAxis = getChart().getXYPlot().getDomainAxis();
		final ValueAxis rangeAxis = getChart().getXYPlot().getRangeAxis();

		if (horizontalMaximum - domainAxis.getRange().getUpperBound() > 0.001) {
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
		if (verticalMaximum - rangeAxis.getRange().getUpperBound() > 0.001) {
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
