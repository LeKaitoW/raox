package ru.bmstu.rk9.rao.lib.animation;

public interface AnimationFrame {
	public String getTypeName();

	public void draw(AnimationContext context);

	public BackgroundData getBackgroundData();
}
