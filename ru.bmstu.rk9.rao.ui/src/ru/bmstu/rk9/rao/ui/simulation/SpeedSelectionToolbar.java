package ru.bmstu.rk9.rao.ui.simulation;

import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class SpeedSelectionToolbar extends WorkbenchWindowControlContribution {
	private static volatile int speedValue;

	public static int getSpeed() {
		return speedValue;
	}

	public static void setSpeed(int val) {
		speedValue = val < RaoSpeedSelector.MIN_VALUE ? RaoSpeedSelector.MIN_VALUE
				: val > RaoSpeedSelector.MAX ? RaoSpeedSelector.MAX : val;
	}

	public class RaoSpeedSelector extends Composite {
		private static final int MIN = 0;
		private static final int MIN_VALUE = 1;
		private static final int MAX = 100;

		private final Color borderColor;
		private final Color mainColor1;
		private final Color mainColor2;
		private final Color labelColor;

		public RaoSpeedSelector(Composite parent, int style) {
			super(parent, style);

			final Display display = getDisplay();
			borderColor = new Color(display, 0x40, 0x40, 0x40);
			labelColor = new Color(display, 0xFF, 0xFF, 0xFF);
			mainColor1 = new Color(display, 0x64, 0x64, 0xD0);
			mainColor2 = new Color(display, 0x58, 0x58, 0xD0);

			final FontData[] fD = this.getFont().getFontData();
			fD[0].setHeight(8);
			this.setFont(new Font(this.getDisplay(), fD[0]));

			this.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

			addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent e) {
					final Point widgetSize = getSize();
					final int percentage = (int) (100f * speedValue / (MAX - MIN));
					final String text = percentage + "%";
					final Point textSize = e.gc.stringExtent(text);
					final Color originalBackground = e.gc.getBackground();
					final Color originalForeground = e.gc.getForeground();

					e.gc.setBackground(mainColor1);
					e.gc.fillRoundRectangle(1, 1, widgetSize.x - 2,
							widgetSize.y / 2 + 1, 4, 4);
					e.gc.setBackground(mainColor2);
					e.gc.fillRoundRectangle(1, widgetSize.y / 2,
							widgetSize.x - 2, widgetSize.y / 2 - 1, 4, 4);

					e.gc.setBackground(originalBackground);
					e.gc.fillRectangle((int) (2 + percentage / 100d
							* (widgetSize.x - 4)), 0,
							(int) (2 + (100 - percentage) / 100d
									* (widgetSize.x - 4)), widgetSize.y);

					e.gc.setForeground(borderColor);
					e.gc.setLineWidth(1);
					e.gc.drawRoundRectangle(1, 1, widgetSize.x - 3,
							widgetSize.y - 3, 4, 4);

					e.gc.setAlpha(50);
					e.gc.drawRoundRectangle(0, 0, widgetSize.x - 1,
							widgetSize.y - 1, 6, 6);
					e.gc.setAlpha(25);
					e.gc.drawRoundRectangle(2, 2, widgetSize.x - 5,
							widgetSize.y - 5, 2, 2);

					e.gc.setAlpha(150);
					e.gc.setBackground(labelColor);

					e.gc.fillRoundRectangle(
							(widgetSize.x - textSize.x) / 2 - 4,
							(widgetSize.y - textSize.y) / 2, textSize.x + 8,
							textSize.y, 4, 4);

					e.gc.setAlpha(255);
					e.gc.setForeground(originalForeground);
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
					if (e.button == 1)
						RaoSpeedSelector.this.setSpeed(e.x,
								RaoSpeedSelector.this.getSize().x);
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			});

			addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent e) {
					if ((e.stateMask & SWT.BUTTON1) != 0)
						RaoSpeedSelector.this.setSpeed(e.x, speed.getSize().x);
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
			speed.setSelection(val < MIN_VALUE ? MIN_VALUE : val > MAX ? MAX
					: val);
		}

		public void modifySpeed(int value) {
			int val = speedValue + value;
			speed.setSelection(val < MIN_VALUE ? MIN_VALUE : val > MAX ? MAX
					: val);
		}

		public void setSelection(int value) {
			speedValue = value;
			SimulationSynchronizer.setSimulationSpeed(value);
			redraw();
		}

		@Override
		protected void checkSubclass() {
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
