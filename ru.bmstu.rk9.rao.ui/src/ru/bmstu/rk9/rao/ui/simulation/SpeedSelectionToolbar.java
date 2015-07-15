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
import org.eclipse.swt.graphics.GC;
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

		private static final int BORDER_SIZE = 1;
		private static final int MAIN_BORDER_OFFSET = 1;
		private static final int CORNER_ROUNDING = 4;
		private static final int LABEL_OFFSET = 1;
		private static final int SHADOW_ALPHA = 60;
		
		private static final int MEDIUM_COLOR_THRESHOLD = 80;
		private static final int LOW_COLOR_THRESHOLD = 20;

		private final Color borderColor;
		private final Color[] progressColorTop;
		private final Color[] progressColorBottom;
		private final Color labelColor;

		public RaoSpeedSelector(Composite parent, int style) {
			super(parent, style);

			final Display display = getDisplay();
			borderColor = new Color(display, 0x40, 0x40, 0x40);
			labelColor = new Color(display, 0xFF, 0xFF, 0xFF);
			progressColorTop = new Color[] {
					new Color(display, 0x00, 0xB5, 0x00),
					new Color(display, 0xF0, 0xF0, 0x00),
					new Color(display, 0xD0, 0x00, 0x00)
			};
			progressColorBottom = new Color[] {
					new Color(display, 0x00, 0xA6, 0x00),
					new Color(display, 0xD0, 0xD0, 0x00),
					new Color(display, 0xC0, 0x00, 0x00)
			};
			//new Color(display, 0x58, 0x58, 0xD0);

			final FontData[] fontData = this.getFont().getFontData();
			fontData[0].setHeight(8);
			this.setFont(new Font(this.getDisplay(), fontData[0]));

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

					if (percentage == MAX)
					{
						e.gc.setBackground(progressColorTop[0]);
						drawProgress(e.gc, widgetSize, true, false, false, 0);
						e.gc.setBackground(progressColorBottom[0]);
						drawProgress(e.gc, widgetSize, false, true, false, 0);
					}
					else
					{
						int alphaRatio = percentage > MEDIUM_COLOR_THRESHOLD ? 255
								: percentage < LOW_COLOR_THRESHOLD ? 0
										: (int) (255f * (percentage - LOW_COLOR_THRESHOLD)//
										/ (MEDIUM_COLOR_THRESHOLD - LOW_COLOR_THRESHOLD));

						e.gc.setBackground(progressColorTop[2]);
						drawProgress(e.gc, widgetSize, true, false, false, 0);
						e.gc.setAlpha(alphaRatio);
						e.gc.setBackground(progressColorTop[1]);
						drawProgress(e.gc, widgetSize, true, false, false, 0);
						
						e.gc.setAlpha(255);
						e.gc.setBackground(progressColorBottom[2]);
						drawProgress(e.gc, widgetSize, false, true, false, 0);
						e.gc.setAlpha(alphaRatio);
						e.gc.setBackground(progressColorBottom[1]);
						drawProgress(e.gc, widgetSize, false, true, false, 0);
						
						e.gc.setAlpha(255);
					}					

					e.gc.setBackground(originalBackground);
					int progressOffset = BORDER_SIZE
							+ (int) (percentage / 100d * (widgetSize.x - 4 * BORDER_SIZE));
					drawProgress(e.gc, widgetSize, true, true, true,
							progressOffset);

					e.gc.setLineWidth(BORDER_SIZE);
					e.gc.setForeground(borderColor);

					e.gc.setAlpha(SHADOW_ALPHA);
					drawBorder(e.gc, widgetSize, 0);

					e.gc.setAlpha(SHADOW_ALPHA / 2);
					drawBorder(e.gc, widgetSize, 2);

					e.gc.setAlpha(255);
					drawBorder(e.gc, widgetSize, 1);

					e.gc.setAlpha(150);
					e.gc.setBackground(labelColor);

					e.gc.fillRoundRectangle((widgetSize.x - textSize.x) / 2 - 4
							* LABEL_OFFSET, (widgetSize.y - textSize.y) / 2,
							textSize.x + 8 * LABEL_OFFSET, textSize.y,
							CORNER_ROUNDING, CORNER_ROUNDING);

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
						setSpeedWithBordersCorrected(e.x, speed.getSize().x);
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			});

			addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent e) {
					if ((e.stateMask & SWT.BUTTON1) != 0)
						setSpeedWithBordersCorrected(e.x, speed.getSize().x);
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

		private void setSpeedWithBordersCorrected(int x, int width) {
			int offset = (MAIN_BORDER_OFFSET + 1) * BORDER_SIZE;
			int reducedSize = width - 2 * offset;
			RaoSpeedSelector.this.setSpeed(x - offset, reducedSize);
		}

		private void drawProgress(final GC gc, final Point widgetSize,
				boolean drawTop, boolean drawBottom, boolean ignoreCorners,
				final int startOffset) {
			int start = drawTop ? 0 : 1;
			int stop = drawBottom ? 2 : 1;

			for (int i = start; i < stop; i++)
				gc.fillRoundRectangle(MAIN_BORDER_OFFSET * BORDER_SIZE
						+ startOffset, MAIN_BORDER_OFFSET * BORDER_SIZE
						* (1 - i) + BORDER_SIZE / 2 + i * widgetSize.y / 2,
						widgetSize.x - 2 * MAIN_BORDER_OFFSET * BORDER_SIZE
								- startOffset, widgetSize.y / 2 + (1 - 3 * i)
								* MAIN_BORDER_OFFSET * BORDER_SIZE,
						ignoreCorners ? 0 : CORNER_ROUNDING + BORDER_SIZE
								* MAIN_BORDER_OFFSET, ignoreCorners ? 0
								: CORNER_ROUNDING + BORDER_SIZE
										* MAIN_BORDER_OFFSET);
		}

		private void drawBorder(final GC gc, final Point widgetSize,
				final int offset) {
			gc.drawRoundRectangle(
					offset * BORDER_SIZE + BORDER_SIZE / 2,
					offset * BORDER_SIZE + BORDER_SIZE / 2,
					widgetSize.x - (2 * offset + 1) * BORDER_SIZE,
					widgetSize.y - (2 * offset + 1) * BORDER_SIZE,
					Math.max(CORNER_ROUNDING + BORDER_SIZE * 2
							* (MAIN_BORDER_OFFSET - offset), 0),
					Math.max(CORNER_ROUNDING + BORDER_SIZE * 2
							* (MAIN_BORDER_OFFSET - offset), 0));
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

		private void setSelection(int value) {
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
		gd.widthHint = 100 + 4 * RaoSpeedSelector.BORDER_SIZE;
		gd.heightHint = 24;

		speed = new RaoSpeedSelector(container, SWT.SMOOTH);
		speed.setLayoutData(gd);
		speed.setSpeed(speedValue, 100);
		speed.setEnabled(true);

		return container;
	}
}
