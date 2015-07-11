package ru.bmstu.rk9.rao.lib.animation;


public interface AnimationFrame {
	public String getName();

	public void draw(AnimationContext context);

	/**
	 * Returns size and color of frame's background packed in array.
	 * Data format is<br>
	 * <blockquote><pre>{@code [width, height, R, G, B]}</pre></blockquote>
	 * <i>Note: background image should be drawn in </i>{@code draw(...)}
	 * <i>method by the frame itself.</i>
	 */
	public int[] getBackgroundData();
}
