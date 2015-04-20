package ru.bmstu.rk9.rdo.lib;

public interface AnimationContext {
	public enum Alignment {
		LEFT, CENTER, RIGHT
	}

	public void drawText(int x, int y, int width, int height,
			int[] backgroundColour, int[] textColor, Alignment alignment,
			String text);

	public void drawRectangle(int x, int y, int width, int height,
			int[] backgroundColour, int[] borderColour);

	public void drawLine(int x1, int y1, int x2, int y2, int[] lineColour);

	public void drawCircle(int x, int y, int radius, int[] backgroundColour,
			int[] borderColour);

	public void drawEllipse(int x, int y, int width, int height,
			int[] backgroundColour, int[] borderColour);

	public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3,
			int[] backgroundColour, int[] borderColour);
}
