package ru.bmstu.rk9.rao.ui.plot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.jfree.chart.axis.ValueAxis;
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

	class PlotKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.SHIFT) {
				setRangeZoomable(true);

			} else if (e.keyCode == SWT.CTRL) {
				setDomainZoomable(true);

			}
			if (e.keyCode == SWT.SPACE) {
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

	class PlotMouseWheelListeneR implements MouseWheelListener {

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
		addMouseWheelListener(new PlotMouseWheelListeneR());
		setZoomInFactor(0.75);
		setZoomOutFactor(1.25);
		setDomainZoomable(false);
		setRangeZoomable(false);
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
