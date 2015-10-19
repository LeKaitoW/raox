package ru.bmstu.rk9.rao.ui.process.terminate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

public class TerminateFigure extends Figure {

	public TerminateFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		setBackgroundColor(ColorConstants.red);
		setOpaque(true);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int centerY = rectangle.y + (rectangle.height - 10) / 2;
		int centerX = rectangle.x + rectangle.width / 2;
		int offsetInner = rectangle.width / 5;
		int offsetOuter = rectangle.width * 2 / 5;

		List<Integer> pointsSequence = new ArrayList<Integer>();
		pointsSequence.add(0);
		pointsSequence.add(offsetInner);
		pointsSequence.add(offsetOuter);
		pointsSequence.add(offsetInner);
		pointsSequence.add(offsetOuter);
		pointsSequence.add(offsetInner);

		for (int i = 0; i < 12; i++) {
			int xSign = i < 6 ? 1 : -1;
			int ySign = i >= 3 && i < 9 ? 1 : -1;
			points.addPoint(centerX + xSign * pointsSequence.get(i % 6),
					centerY + ySign * pointsSequence.get((i + 3) % 6));
		}

		graphics.fillPolygon(points);

		Font oldFont = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme().getFontRegistry()
				.get(PreferenceConstants.EDITOR_TEXT_FONT);
		FontData[] fontData = oldFont.getFontData();
		fontData[0].setHeight(6);
		Font font = new Font(oldFont.getDevice(), fontData);
		graphics.setFont(font);
		graphics.drawText("Terminate", rectangle.x + 2, rectangle.y + 48);
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
}
