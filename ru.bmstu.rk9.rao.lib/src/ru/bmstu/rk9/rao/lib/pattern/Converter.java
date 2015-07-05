package ru.bmstu.rk9.rao.lib.pattern;

public interface Converter<R, P> {
	public void run(R resources, P parameters);
}
