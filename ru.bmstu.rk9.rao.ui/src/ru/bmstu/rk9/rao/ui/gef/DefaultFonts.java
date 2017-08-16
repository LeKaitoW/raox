package ru.bmstu.rk9.rao.ui.gef;

import org.eclipse.swt.widgets.Display;

import ru.bmstu.rk9.rao.ui.gef.font.Font;

public final class DefaultFonts {

	/**
	 * Экземпляры запрещены
	 */
	private DefaultFonts() {
	}

	public static final Font DEFAULT_FONT = new Font(
			Display.getDefault().getSystemFont());
}
