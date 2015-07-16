package ru.bmstu.rk9.rao.ui.simulation;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class SpeedSelectionToolbar extends WorkbenchWindowControlContribution {
	private static volatile int speedValue;

	public static int getSpeed() {
		return speedValue;
	}

	public static void setSpeed(int val) {
		speedValue = val < RaoSpeedSelector.MIN_VALUE ? RaoSpeedSelector.MIN_VALUE
				: val > RaoSpeedSelector.MAX_VALUE ? RaoSpeedSelector.MAX_VALUE : val;
	}

	public static class RaoSpeedSelector extends Composite {
		private static final int MIN_VALUE = 1;
		private static final int MAX_VALUE = 100;

		private static final int BORDER_SIZE = 1;
		private static final int MAIN_BORDER_OFFSET = 1;
		private static final int CORNER_ROUNDING = 6;
		private static final int LABEL_OFFSET = 1;
		private static final int SHADOW_ALPHA = 60;

		private static final int MEDIUM_COLOR_THRESHOLD = 80;
		private static final int LOW_COLOR_THRESHOLD = 10;

		private final Color borderColor;
		private final Color[] progressColorTop;
		private final Color[] progressColorBottom;
		private final Color labelBackgroundColor;
		private final Color labelForegroundColor;

		public RaoSpeedSelector(Composite parent, int style) {
			super(parent, style);

			final Display display = getDisplay();
			borderColor = new Color(display, 0x40, 0x40, 0x40);
			labelBackgroundColor = display.getSystemColor(SWT.COLOR_WHITE);
			labelForegroundColor = display.getSystemColor(SWT.COLOR_BLACK);
			progressColorTop = new Color[] { new Color(display, 0x00, 0xB5, 0x00), new Color(display, 0xF0, 0xF0, 0x00),
					new Color(display, 0xD0, 0x00, 0x00) };
			progressColorBottom = new Color[] { new Color(display, 0x00, 0xA6, 0x00),
					new Color(display, 0xD0, 0xD0, 0x00), new Color(display, 0xC0, 0x00, 0x00) };

			final FontData[] fontData = getFont().getFontData().clone();
			fontData[0].setHeight(8);
			setFont(new Font(display, fontData[0]));

			addPaintListener(new PaintListener() {
				@Override
				public void paintControl(final PaintEvent event) {
					GC gc = event.gc;
					gc.setAntialias(SWT.ON);
					final Rectangle widgetArea = getClientArea();
					final int percentage = (int) (100f * speedValue / (MAX_VALUE - 0));
					final String text = percentage + "%";
					final Point textSize = gc.stringExtent(text);
					final Color originalBackground = gc.getBackground();

					if (percentage == MAX_VALUE) {
						gc.setBackground(progressColorTop[0]);
						drawProgress(gc, widgetArea, ProgressPaintMode.TOP, 0);
						gc.setBackground(progressColorBottom[0]);
						drawProgress(gc, widgetArea, ProgressPaintMode.BOTTOM, 0);
					} else {
						int alphaRatio;
						if (percentage > MEDIUM_COLOR_THRESHOLD)
							alphaRatio = 255;
						else if (percentage < LOW_COLOR_THRESHOLD)
							alphaRatio = 0;
						else
							alphaRatio = (int) (255f * (percentage - LOW_COLOR_THRESHOLD)
									/ (MEDIUM_COLOR_THRESHOLD - LOW_COLOR_THRESHOLD));

						gc.setBackground(progressColorTop[2]);
						drawProgress(gc, widgetArea, ProgressPaintMode.TOP, 0);
						gc.setAlpha(alphaRatio);
						gc.setBackground(progressColorTop[1]);
						drawProgress(gc, widgetArea, ProgressPaintMode.TOP, 0);

						gc.setAlpha(255);
						gc.setBackground(progressColorBottom[2]);
						drawProgress(gc, widgetArea, ProgressPaintMode.BOTTOM, 0);
						gc.setAlpha(alphaRatio);
						gc.setBackground(progressColorBottom[1]);
						drawProgress(gc, widgetArea, ProgressPaintMode.BOTTOM, 0);

						gc.setAlpha(255);
					}

					gc.setBackground(originalBackground);
					int progressOffset = BORDER_SIZE + (int) (percentage / 100d * (widgetArea.width - 4 * BORDER_SIZE));
					drawProgress(gc, widgetArea, ProgressPaintMode.ALL_NO_CORNERS, progressOffset);

					gc.setLineWidth(BORDER_SIZE);
					gc.setForeground(borderColor);

					gc.setAlpha(SHADOW_ALPHA);
					drawBorder(gc, widgetArea, 0);

					gc.setAlpha(SHADOW_ALPHA / 2);
					drawBorder(gc, widgetArea, 2);

					gc.setAlpha(255);
					drawBorder(gc, widgetArea, 1);

					drawLabel(gc, widgetArea, text, textSize);
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

		public static enum ProgressPaintMode {
			TOP, BOTTOM, ALL_NO_CORNERS
		}

		private void setSpeedWithBordersCorrected(int x, int width) {
			int offset = (MAIN_BORDER_OFFSET + 1) * BORDER_SIZE;
			int reducedSize = width - 2 * offset;
			RaoSpeedSelector.this.setSpeed(x - offset, reducedSize);
		}

		private void drawProgress(final GC gc, final Rectangle widgetArea, ProgressPaintMode paintMode,
				final int startOffset) {
			int start = paintMode != ProgressPaintMode.BOTTOM ? 0 : 1;
			int stop = paintMode != ProgressPaintMode.TOP ? 2 : 1;

			for (int i = start; i < stop; i++) {
				int x = MAIN_BORDER_OFFSET * BORDER_SIZE + startOffset;
				int y = MAIN_BORDER_OFFSET * BORDER_SIZE * (1 - i) + BORDER_SIZE / 2 + i * widgetArea.height / 2;
				int width = widgetArea.width - 2 * MAIN_BORDER_OFFSET * BORDER_SIZE - startOffset;
				int height = widgetArea.height / 2 + (1 - 2 * i) * MAIN_BORDER_OFFSET * BORDER_SIZE;
				int cornerRadius = paintMode == ProgressPaintMode.ALL_NO_CORNERS ? 0
						: CORNER_ROUNDING + BORDER_SIZE * MAIN_BORDER_OFFSET;
				gc.fillRoundRectangle(x, y, width, height, cornerRadius, cornerRadius);
			}
		}

		private void drawBorder(final GC gc, final Rectangle widgetArea, final int offset) {
			int x = offset * BORDER_SIZE + BORDER_SIZE / 2;
			int y = offset * BORDER_SIZE + BORDER_SIZE / 2;
			int width = widgetArea.width - (2 * offset + 1) * BORDER_SIZE;
			int height = widgetArea.height - (2 * offset + 1) * BORDER_SIZE;
			int cornerRadius = Math.max(CORNER_ROUNDING + BORDER_SIZE * 2 * (MAIN_BORDER_OFFSET - offset), 0);
			gc.drawRoundRectangle(x, y, width, height, cornerRadius, cornerRadius);
		}

		private void drawLabel(final GC gc, final Rectangle widgetArea, final String text, final Point textSize) {
			gc.setAlpha(150);
			gc.setBackground(labelBackgroundColor);

			int x = widgetArea.width / 2 - textSize.x / 2 - 4 * LABEL_OFFSET;
			int y = widgetArea.height / 2 - textSize.y / 2;
			int width = textSize.x + 8 * LABEL_OFFSET;
			int height = textSize.y;
			gc.fillRoundRectangle(x, y, width, height, CORNER_ROUNDING, CORNER_ROUNDING);

			gc.setAlpha(255);
			gc.setForeground(labelForegroundColor);

			x = widgetArea.width / 2 - textSize.x / 2;
			y = widgetArea.height / 2 - textSize.y / 2;
			gc.drawString(text, x, y, true);
		}

		public void setSpeed(int part, int all) {
			int val = part * MAX_VALUE / all;
			speed.setSelection(val < MIN_VALUE ? MIN_VALUE : val > MAX_VALUE ? MAX_VALUE : val);
		}

		public void modifySpeed(int value) {
			int val = speedValue + value;
			speed.setSelection(val < MIN_VALUE ? MIN_VALUE : val > MAX_VALUE ? MAX_VALUE : val);
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
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = 0;
		gl.marginLeft = 4;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		parent.setLayout(gl);

		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		gd.widthHint = 100 + 4 * RaoSpeedSelector.BORDER_SIZE;
		gd.heightHint = 24;

		speed = new RaoSpeedSelector(parent, SWT.DOUBLE_BUFFERED);
		speed.setLayoutData(gd);
		speed.setSpeed(speedValue, 100);
		speed.setEnabled(true);

		return speed;
	}
}
