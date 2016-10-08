package ru.bmstu.rk9.rao.lib.animation;

public interface AnimationContext {
	public void drawText(String text, int x, int y, RaoColor textColor, int width, Alignment alignment);

	public void drawText(String text, int x, int y, RaoColor textColor);

	public void drawText(String text, int x, int y);

	public void drawRectangle(int x, int y, int width, int height, RaoColor backgroundColour, RaoColor borderColour);

	public void drawRectangle(int x, int y, int width, int height);

	public void drawLine(int x1, int y1, int x2, int y2, RaoColor lineColour);

	public void drawLine(int x1, int y1, int x2, int y2);

	public void drawCircle(int x, int y, int radius, RaoColor backgroundColour, RaoColor borderColour);

	public void drawCircle(int x, int y, int radius);

	public void drawEllipse(int x, int y, int width, int height, RaoColor backgroundColour, RaoColor borderColour);

	public void drawEllipse(int x, int y, int width, int height);

	public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, RaoColor backgroundColour,
			RaoColor borderColour);

	public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3);

	public void drawImage(String name, int x, int y);

	void drawImage(String name, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth,
			int destHeight);
}
