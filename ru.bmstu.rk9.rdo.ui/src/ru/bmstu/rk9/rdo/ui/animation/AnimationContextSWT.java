package ru.bmstu.rk9.rdo.ui.animation;

import java.util.HashMap;

import org.eclipse.swt.SWT;

import org.eclipse.swt.graphics.GC;

import org.eclipse.swt.widgets.Display;

import org.eclipse.swt.graphics.Image;

import org.eclipse.swt.graphics.Color;

import org.eclipse.swt.graphics.Rectangle;

import ru.bmstu.rk9.rdo.lib.AnimationFrame;
import ru.bmstu.rk9.rdo.lib.AnimationContext;

public class AnimationContextSWT implements AnimationContext
{
	private Display display;

	AnimationContextSWT(Display display)
	{
		this.display = display;
	}

	private GC paintContext;

	private HashMap<AnimationFrame, Image> storedFrames
		= new HashMap<AnimationFrame, Image>();

	void drawBackground(int[] backgroundData)
	{
		paintContext.setAlpha(255);

		Color backgroundColor = createColor(backgroundData, 2);

		paintContext.setBackground(backgroundColor);
		paintContext.fillRectangle(0, 0, backgroundData[0], backgroundData[1]);

		backgroundColor.dispose();
	}

	void drawFrame(GC gc, AnimationFrame frame)
	{
		Image image = drawFrameBuffer(frame);

		gc.drawImage(image, 0, 0);

		image.dispose();
	}

	void storeFrame(AnimationFrame frame)
	{
		Image lastStored = storedFrames.get(frame);

		if(lastStored != null)
			lastStored.dispose();

		storedFrames.put(frame, drawFrameBuffer(frame));
	}

	private Image drawFrameBuffer(AnimationFrame frame)
	{
		int[] backgroundData = frame.getBackgroundData();

		Rectangle backgroundRectangle =
			new Rectangle(0, 0, backgroundData[0], backgroundData[1]);

		Image image = new Image(display, backgroundRectangle);

		paintContext = new GC(image);
		paintContext.setAntialias(SWT.ON);

		drawBackground(backgroundData);

		frame.draw(this);

		paintContext.dispose();

		return image;
	}

	void restoreFrame(GC gc, AnimationFrame frame)
	{
		Image image = storedFrames.get(frame);

		if(image != null)
			gc.drawImage(image, 0, 0);
	}

	@Override
	public void drawText
	(
		int x, int y,
		int width, int height,
		int[] backgroundColour,
		int[] textColor,
		Alignment alignment,
		String text
	)
	{
		paintContext.setAlpha(backgroundColour[3]);

		Color backgroundColor = createColor(backgroundColour, 0);

		paintContext.setBackground(backgroundColor);
		paintContext.fillRectangle(x, y, width, height);

		paintContext.setAlpha(textColor[3]);

		Color foregroundColor = createColor(textColor, 0);

		paintContext.setForeground(foregroundColor);
		paintContext.drawText(text, x, y, true);

		backgroundColor.dispose();
		foregroundColor.dispose();
	}

	@Override
	public void drawRectangle
	(
		int x, int y,
		int width, int height,
		int[] backgroundColour,
		int[] borderColour
	)
	{
		paintContext.setAlpha(backgroundColour[3]);
		
		Color backgroundColor = createColor(backgroundColour, 0);

		paintContext.setBackground(backgroundColor);
		paintContext.fillRectangle(x, y, width, height);

		paintContext.setAlpha(borderColour[3]);

		Color borderColor = createColor(borderColour, 0);

		paintContext.setBackground(borderColor);
		paintContext.drawRectangle(x, y, width, height);

		backgroundColor.dispose();
		borderColor.dispose();
	}

	@Override
	public void drawLine
	(
		int x1, int y1,
		int x2, int y2,
		int[] lineColour
	)
	{
		paintContext.setAlpha(lineColour[3]);

		Color lineColor = createColor(lineColour, 0);

		paintContext.setBackground(lineColor);
		paintContext.drawLine(x1, y1, x2, y2);

		lineColor.dispose();
	}

	@Override
	public void drawCircle
	(
		int x, int y,
		int radius,
		int[] backgroundColour,
		int[] borderColour
	)
	{
		paintContext.setAlpha(backgroundColour[3]);

		Color backgroundColor = createColor(backgroundColour, 0);

		paintContext.setBackground(backgroundColor);
		paintContext.fillOval(x - radius / 2, y - radius / 2, radius, radius);

		paintContext.setAlpha(borderColour[3]);

		Color borderColor = createColor(borderColour, 0);

		paintContext.setBackground(borderColor);
		paintContext.drawOval(x - radius / 2, y - radius / 2, radius, radius);

		backgroundColor.dispose();
		borderColor.dispose();
	}

	@Override
	public void drawEllipse
	(
		int x, int y,
		int width, int height,
		int[] backgroundColour,
		int[] borderColour
	)
	{
		paintContext.setAlpha(backgroundColour[3]);

		Color backgroundColor = createColor(backgroundColour, 0);

		paintContext.setBackground(backgroundColor);
		paintContext.fillOval(x, y, width, height);

		paintContext.setAlpha(borderColour[3]);

		Color borderColor = createColor(borderColour, 0);

		paintContext.setBackground(borderColor);
		paintContext.drawOval(x, y, width, height);

		backgroundColor.dispose();
		borderColor.dispose();
}

	@Override
	public void drawTriangle
	(
		int x1, int y1,
		int x2, int y2,
		int x3, int y3,
		int[] backgroundColour,
		int[] borderColour
	)
	{
		paintContext.setAlpha(backgroundColour[3]);

		Color backgroundColor = createColor(backgroundColour, 0);

		paintContext.setBackground(backgroundColor);
		paintContext.fillPolygon(new int[] {x1, y1, x2, y2, x3, y3});

		paintContext.setAlpha(borderColour[3]);

		Color borderColor = createColor(borderColour, 0);

		paintContext.setBackground(borderColor);
		paintContext.drawPolygon(new int[] {x1, y1, x2, y2, x3, y3});

		backgroundColor.dispose();
		borderColor.dispose();
	}

	Color createColor(int[] components, int offset)
	{
		return new Color
		(
			display,
			components[offset],
			components[offset + 1],
			components[offset + 2]
		);
	}
}
