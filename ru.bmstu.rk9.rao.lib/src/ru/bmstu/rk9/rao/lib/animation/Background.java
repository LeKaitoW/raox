package ru.bmstu.rk9.rao.lib.animation;

public class Background {
	public Background(int width, int height, RaoColor color) {
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public Background(int width, int height) {
		this(width, height, RaoColor.WHITE);
	}

	public final int width;
	public final int height;
	public final RaoColor color;
}
