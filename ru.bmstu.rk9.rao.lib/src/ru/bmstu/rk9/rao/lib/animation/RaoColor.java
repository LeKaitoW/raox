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

	public static final RaoColor BLACK = new RaoColor(0, 0, 0);
	public static final RaoColor DARK_RED = new RaoColor(0x80, 0, 0);
	public static final RaoColor DARK_GREEN = new RaoColor(0, 0x80, 0);
	public static final RaoColor DARK_YELLOW = new RaoColor(0x80, 0x80, 0);
	public static final RaoColor DARK_BLUE = new RaoColor(0, 0, 0x80);
	public static final RaoColor DARK_MAGENTA = new RaoColor(0x80, 0, 0x80);
	public static final RaoColor DARK_CYAN = new RaoColor(0, 0x80, 0x80);
	public static final RaoColor GRAY = new RaoColor(0xC0, 0xC0, 0xC0);
	public static final RaoColor DARK_GRAY = new RaoColor(0x80, 0x80, 0x80);
	public static final RaoColor RED = new RaoColor(0xFF, 0, 0);
	public static final RaoColor GREEN = new RaoColor(0, 0xFF, 0);
	public static final RaoColor YELLOW = new RaoColor(0xFF, 0xFF, 0);
	public static final RaoColor BLUE = new RaoColor(0, 0, 0xFF);
	public static final RaoColor MAGENTA = new RaoColor(0xFF, 0, 0xFF);
	public static final RaoColor CYAN = new RaoColor(0, 0xFF, 0xFF);
	public static final RaoColor WHITE = new RaoColor(0xFF, 0xFF, 0xFF);
}
