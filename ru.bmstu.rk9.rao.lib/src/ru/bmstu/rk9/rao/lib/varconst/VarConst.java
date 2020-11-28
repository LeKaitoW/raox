package ru.bmstu.rk9.rao.lib.varconst;

import java.util.HashMap;

import ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression;

public abstract class VarConst {
	private Double	start;
	private Double	stop;
	private Double	step;
	private LambdaExpression lambda;
	
	public abstract String getName();
	public abstract Boolean checkValue(HashMap<String, Double> args);
	
//	getAllDependencies method creates in VarConstCompiler.xtend
}
