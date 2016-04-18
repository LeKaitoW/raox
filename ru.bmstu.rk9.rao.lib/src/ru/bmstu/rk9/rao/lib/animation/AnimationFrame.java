package ru.bmstu.rk9.rao.lib.animation;

public abstract class AnimationFrame {
	public AnimationFrame() {
		init();
	}

	protected abstract void init();

	public abstract String getTypeName();

	public abstract void draw(AnimationContext context);

	public final Background getBackground() {
		return background;
	}

	public final void setBackground(Background background) {
		this.background = background;
	}

	private Background background = new Background(500, 500, RaoColor.WHITE);
}
