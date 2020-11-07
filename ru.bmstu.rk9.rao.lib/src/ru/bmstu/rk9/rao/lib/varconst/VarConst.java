package ru.bmstu.rk9.rao.lib.varconst;

import java.util.ArrayList;
import java.util.Arrays;

import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public abstract class VarConst {
	private Double	start;
	private Double	stop;
	private Double	step;
	private LambdaExpression lambda;
	
	public abstract String getName();
//	checkValue method u dont need
}
