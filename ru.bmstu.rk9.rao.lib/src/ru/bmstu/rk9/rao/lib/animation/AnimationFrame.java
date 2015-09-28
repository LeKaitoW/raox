package ru.bmstu.rk9.rao.lib.animation;

public interface AnimationFrame {
	public String getName();

	public void draw(AnimationContext context);

	public BackgroundData getBackgroundData();
}
