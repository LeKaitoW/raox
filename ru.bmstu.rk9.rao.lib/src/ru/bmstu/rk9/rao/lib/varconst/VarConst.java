package ru.bmstu.rk9.rao.lib.varconst;

import java.util.ArrayList;
import java.util.HashMap;

import ru.bmstu.rk9.rao.lib.lambdaexpression.LambdaExpression;

public abstract class VarConst {
	private Double	start;
	private Double	stop;
	private Double	step;
	private LambdaExpression lambda;
	
	public abstract String getName();
	public abstract Boolean checkValue(HashMap<String, Double> args);
	public abstract ArrayList<String> getallDependencies();
	
	public Double getStart() {
		return this.start;
	}
	
	public Double getStop() {
		return this.stop;
	}
	
	public Double getStep() {
		return this.step;
	}
}
