package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;

public class ProcessFigure extends Figure {

	protected Label label = new Label();
	private boolean isNameVisible = true;

	public ProcessFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		Font oldFont = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme().getFontRegistry()
				.get(PreferenceConstants.EDITOR_TEXT_FONT);
		FontData[] fontData = oldFont.getFontData();
		fontData[0].setHeight(6);
		font = new Font(oldFont.getDevice(), fontData);
		add(label);
		setOpaque(true);
	}

	private Font font;

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}

	@Override
	public Font getFont() {
		return font;
	}

	public void setFigureNameVisible(boolean visible) {
		isNameVisible = visible;
		paintName(this.getBounds());
	}

	protected void paintName(Rectangle rectangle) {
		Rectangle relativeRectangle = rectangle.getCopy();
		relativeRectangle.setX(0);
		relativeRectangle.setY(rectangle.height * 2 / 5);

		label.setFont(getFont());
		add(label);
		setConstraint(label, relativeRectangle);
		label.setVisible(isNameVisible);
	}
}
