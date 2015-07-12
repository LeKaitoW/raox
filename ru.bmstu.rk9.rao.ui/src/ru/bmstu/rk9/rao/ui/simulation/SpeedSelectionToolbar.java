package ru.bmstu.rk9.rao.ui.simulation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class SpeedSelectionToolbar extends WorkbenchWindowControlContribution {
	private static volatile int speedValue;

	public static int getSpeed() {
		return speedValue;
	}

	public static void setSpeed(int val) {
		speedValue = val < RaoSpeedSelector.MIN ? RaoSpeedSelector.MIN
				: val > RaoSpeedSelector.MAX ? RaoSpeedSelector.MAX : val;
	}

	public class RaoSpeedSelector extends ProgressBar {
		private static final int MIN = 1;
		private static final int MAX = 100;

		public RaoSpeedSelector(Composite parent, int style) {
			super(parent, style);

			this.setMinimum(0);
			this.setMaximum(MAX);

			FontData[] fontData = this.getFont().getFontData();
			fontData[0].setHeight(8);
			this.setFont(new Font(this.getDisplay(), fontData[0]));

			this.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

			addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent e) {
					final Point widgetSize = getSize();
					final int percentage = (int) (100f * getSelection() / (getMaximum() - getMinimum()));
					final String text = percentage + "%";
					final Point textSize = e.gc.stringExtent(text);

					int alpha = e.gc.getAlpha();
					e.gc.setAlpha(150);

					e.gc.fillRoundRectangle(
							(widgetSize.x - textSize.x) / 2 - 4,
							(widgetSize.y - textSize.y) / 2, textSize.x + 8,
							textSize.y, 4, 4);

					e.gc.setAlpha(alpha);
					e.gc.drawString(text, (widgetSize.x - textSize.x) / 2,
							(widgetSize.y - textSize.y) / 2, true);
				}
			});

			addMouseListener(new MouseListener() {
				@Override
				public void mouseUp(MouseEvent e) {
				}

				@Override
				public void mouseDown(MouseEvent e) {
					if (e.button == 1) {
						RaoSpeedSelector.this.setSpeed(e.x,
								RaoSpeedSelector.this.getSize().x);
					}
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			});

			addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent e) {
					if ((e.stateMask & SWT.BUTTON1) != 0) {
						RaoSpeedSelector.this.setSpeed(e.x, speed.getSize().x);
					}
				}
			});

			addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseScrolled(MouseEvent e) {
					RaoSpeedSelector.this.modifySpeed((e.count > 0) ? 5 : -5);
				}
			});

			setToolTipText("Simulation Speed");
		}

		public void setSpeed(int part, int all) {
			int val = part * MAX / all;
			speed.setSelection(val < MIN ? MIN : val > MAX ? MAX : val);
		}

		public void modifySpeed(int value) {
			int val = this.getSelection() + value;
			speed.setSelection(val < MIN ? MIN : val > MAX ? MAX : val);
		}

		@Override
		public void setSelection(int value) {
			disableAnimation();
			super.setSelection(value);
			speedValue = value;
			SimulationSynchronizer.setSimulationSpeed(value);
			enableAnimation();
		}

		@Override
		protected void checkSubclass() {
		}

		private final void disableAnimation() {
			setRedraw(false);
			setState(SWT.PAUSED);
		}

		private final void enableAnimation() {
			setState(SWT.NORMAL);
			setRedraw(true);
		}
	}

	private static RaoSpeedSelector speed;

	@Override
	protected Control createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = 0;
		gl.marginLeft = 4;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		container.setLayout(gl);

		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		gd.widthHint = 100;
		gd.heightHint = 24;

		speed = new RaoSpeedSelector(container, SWT.SMOOTH);
		speed.setLayoutData(gd);
		speed.setSpeed(speedValue, 100);
		speed.setEnabled(true);

		return container;
	}
}
