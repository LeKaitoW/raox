package ru.bmstu.rk9.rao.lib.animation;

public class RaoColor {
	public RaoColor(int r, int g, int b) {
		this(r, g, b, 0xFF);
	}

	public RaoColor(int r, int g, int b, int alpha) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.alpha = alpha;
	}

	public final int alpha;
	public final int r;
	public final int g;
	public final int b;

	public static final RaoColor COLOR_BLACK = new RaoColor(0, 0, 0);
	public static final RaoColor COLOR_DARK_RED = new RaoColor(0x80, 0, 0);
	public static final RaoColor COLOR_DARK_GREEN = new RaoColor(0, 0x80, 0);
	public static final RaoColor COLOR_DARK_YELLOW = new RaoColor(0x80, 0x80, 0);
	public static final RaoColor COLOR_DARK_BLUE = new RaoColor(0, 0, 0x80);
	public static final RaoColor COLOR_DARK_MAGENTA = new RaoColor(0x80, 0,
			0x80);
	public static final RaoColor COLOR_DARK_CYAN = new RaoColor(0, 0x80, 0x80);
	public static final RaoColor COLOR_GRAY = new RaoColor(0xC0, 0xC0, 0xC0);
	public static final RaoColor COLOR_DARK_GRAY = new RaoColor(0x80, 0x80,
			0x80);
	public static final RaoColor COLOR_RED = new RaoColor(0xFF, 0, 0);
	public static final RaoColor COLOR_GREEN = new RaoColor(0, 0xFF, 0);
	public static final RaoColor COLOR_YELLOW = new RaoColor(0xFF, 0xFF, 0);
	public static final RaoColor COLOR_BLUE = new RaoColor(0, 0, 0xFF);
	public static final RaoColor COLOR_MAGENTA = new RaoColor(0xFF, 0, 0xFF);
	public static final RaoColor COLOR_CYAN = new RaoColor(0, 0xFF, 0xFF);
	public static final RaoColor COLOR_WHITE = new RaoColor(0xFF, 0xFF, 0xFF);
}
