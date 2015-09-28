package ru.bmstu.rk9.rao.lib.animation;

public class RaoColor {
	public enum ColorValue {
		UNDEFINED, WHITE, BLACK
	};

	public RaoColor(ColorValue value) {
		this.value = value;

		this.r = -1;
		this.g = -1;
		this.b = -1;
		this.alpha = 255;
	}

	public RaoColor(int r, int g, int b, int alpha) {
		this.value = ColorValue.UNDEFINED;

		this.r = r;
		this.g = g;
		this.b = b;
		this.alpha = alpha;
	}

	public final int alpha;
	public final int r;
	public final int g;
	public final int b;
	public final ColorValue value;
}
