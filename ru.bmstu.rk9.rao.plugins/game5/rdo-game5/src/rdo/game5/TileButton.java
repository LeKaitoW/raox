package rdo.game5;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class TileButton extends Composite {

	public TileButton(Composite parent, int style, String number) {
		super(parent, style);
		this.number = number;
		addPaintListener(tilePaintListener);
	}

	private String number;

	private final PaintListener tilePaintListener = new PaintListener() {
		@Override
		public void paintControl(PaintEvent event) {
			GC gc = event.gc;
			final Display display = getDisplay();
			final Rectangle tile = getClientArea();
			final Color black = new Color(display, 0x00, 0x00, 0x00);
			final Color red = new Color(display, 0xff, 0x19, 0x19);
			final FontRegistry fontRegistry = PlatformUI.getWorkbench()
					.getThemeManager().getCurrentTheme().getFontRegistry();
			final String fontName = fontRegistry.get(
					PreferenceConstants.EDITOR_TEXT_FONT).getFontData()[0]
					.getName();

			final FontData fontData = new FontData(fontName, 16, SWT.BOLD);
			final Font font = new Font(display, fontData);
			gc.setFont(font);
			final Point textSize = gc.stringExtent(number);

			if (!number.equals(String.valueOf(6))) {
				gc.setForeground(black);
				gc.drawRoundRectangle(2, 2, 60, 60, 5, 5);
				gc.setBackground(red);
				gc.fillRoundRectangle(3, 3, 59, 59, 5, 5);
				int x = tile.width / 2 - textSize.x / 2;
				int y = tile.height / 2 - textSize.y / 2;
				gc.drawString(number, x, y);
			}
		}
	};

	public final void updateTile(String number) {
		this.number = number;
		this.redraw();
		this.update();
	}
}
