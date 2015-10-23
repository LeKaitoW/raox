package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class GenerateFigure extends ProcessFigure {

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int offset = 5;
		int centerY = rectangle.y + (rectangle.height - 10) / 2;
		points.addPoint(rectangle.x + offset, rectangle.y + offset);
		points.addPoint(rectangle.x + rectangle.width - offset, centerY);
		points.addPoint(rectangle.x + offset, rectangle.y + rectangle.height
				- 3 * offset);
		graphics.fillPolygon(points);

		Font oldFont = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme().getFontRegistry()
				.get(PreferenceConstants.EDITOR_TEXT_FONT);
		FontData[] fontData = oldFont.getFontData();
		fontData[0].setHeight(6);
		Font font = new Font(oldFont.getDevice(), fontData);
		graphics.setFont(font);
		graphics.drawText("Generate", rectangle.x + 2, rectangle.y + 48);
	}
}
