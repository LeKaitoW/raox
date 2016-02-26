package ru.bmstu.rk9.rao.lib.runtime;

import java.util.function.Supplier;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import ru.bmstu.rk9.rao.lib.dpt.Logic;

public class RaoFactory {
	public static Logic createLogic(Procedure1<? super Logic> initializer) {
		return createLogic(() -> Double.MAX_VALUE, initializer);
	}

	public static Logic createLogic(double priority, Procedure1<? super Logic> initializer) {
		return createLogic(() -> priority, initializer);
	}

	public static Logic createLogic(Supplier<Double> priority, Procedure1<? super Logic> initializer) {
		return createLogic(priority, () -> true, initializer);
	}

	public static Logic createLogic(double priority, Supplier<Boolean> condition, Procedure1<? super Logic> initializer) {
		return createLogic(() -> priority, condition, initializer);
	}

	public static Logic createLogic(Supplier<Double> priority, Supplier<Boolean> condition, Procedure1<? super Logic> initializer) {
		Logic logic = new Logic(priority, condition);

		if (initializer != null) {
			try {
				initializer.apply(logic);
			} catch (Exception e) {
				return null;
			}
		}

		return logic;
	}
}
