package ru.bmstu.rk9.rao.lib.dpt;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.pattern.Pattern;

public class LogicActivity extends Activity {
	//TODO add initializer constructor/fabric method
	public LogicActivity(Pattern pattern) {
		this(pattern, () -> 0.0);
	}

	public LogicActivity(Pattern pattern, Supplier<Double> priority) {
		this.pattern = pattern;
		this.priority = priority;
	}

	public final void setPriority(Supplier<Double> priority) {
		this.priority = priority;
	}

	public Supplier<Double> priority;
	private final Pattern pattern;

	@Override
	public boolean check() {
		return pattern.selectRelevantResources();
	}

	public void execute() {
		pattern.run();
	}
}
