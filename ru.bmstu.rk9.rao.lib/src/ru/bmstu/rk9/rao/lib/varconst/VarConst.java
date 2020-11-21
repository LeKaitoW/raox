package ru.bmstu.rk9.rao.lib.varconst;

import ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression;

public abstract class VarConst {
	private Double	start;
	private Double	stop;
	private Double	step;
	private LambdaExpression lambda;
	
	public abstract String getName();
//	checkValue method u dont need
}
