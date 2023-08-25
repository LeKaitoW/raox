package ru.bmstu.rk9.rao.ui.gef;

import org.eclipse.jface.resource.FontRegistry;

import ru.bmstu.rk9.rao.ui.gef.font.Font;

public final class DefaultFonts {

	/**
	 * Экземпляры запрещены
	 */
	private DefaultFonts() {
	}

	public static final Font DEFAULT_FONT = new Font(new FontRegistry().defaultFont());
}
