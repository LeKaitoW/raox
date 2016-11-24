package ru.bmstu.rk9.rao.lib.animation;

public abstract class AnimationFrame {
	public AnimationFrame() {
		init();
	}

	protected void init() {
	}

	public void mouseDown(int x, int y, int button, int stateMask) {
	}

	public void mouseUp(int x, int y, int button, int stateMask) {
	}

	public void mouseMove(int x, int y, int button, int stateMask) {
	}

	public void mouseDoubleClick(int x, int y, int button, int stateMask) {
	}

	public void mouseScrolled(int x, int y, int count) {
	}

	public void keyReleased(int keyCode, int stateMask) {
	}

	public void keyPressed(int keyCode, int stateMask) {
	}

	public abstract String getTypeName();

	public void draw(AnimationContext context) {
	}

	public final Background getBackground() {
		return background;
	}

	public final void setBackground(Background background) {
		this.background = background;
	}

	private Background background = new Background(500, 500, RaoColor.WHITE);
}
