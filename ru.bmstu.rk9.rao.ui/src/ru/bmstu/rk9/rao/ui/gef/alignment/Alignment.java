package ru.bmstu.rk9.rao.ui.gef.alignment;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

public enum Alignment {

	FIRST_LINE_START("First line start") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(0, 0);
		}
	},
	PAGE_START("Page start") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width / 2 - newDimension.width / 2, 0);
		}
	},
	FIRST_LINE_END("First line end") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width - newDimension.width, 0);
		}
	},
	LINE_START("Line start") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(0, oldDimension.height / 2 - newDimension.height / 2);
		}
	},
	CENTER("Center") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width / 2 - newDimension.width / 2,
					oldDimension.height / 2 - newDimension.height / 2);
		}
	},
	LINE_END("Line end") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width - newDimension.width,
					oldDimension.height / 2 - newDimension.height / 2);
		}
	},
	LAST_LINE_START("Last line start") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(0, oldDimension.height - newDimension.height);
		}
	},
	PAGE_END("Page end") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width / 2 - newDimension.width / 2,
					oldDimension.height - newDimension.height);
		}
	},
	LAST_LINE_END("Last line end") {
		@Override
		public Point translation(Dimension oldDimension, Dimension newDimension) {
			return new Point(oldDimension.width - newDimension.width, oldDimension.height - newDimension.height);
		}
	};

	private String description;

	private Alignment(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public abstract Point translation(Dimension oldDimension, Dimension newDimension);

	public static final Alignment defaultAlignment = FIRST_LINE_START;
	public static final Alignment defaultForBlockTitleNode = PAGE_END;
	public static final Alignment[] ordered = { FIRST_LINE_START, PAGE_START, FIRST_LINE_END, LINE_START, CENTER,
			LINE_END, LAST_LINE_START, PAGE_END, LAST_LINE_END };

}
