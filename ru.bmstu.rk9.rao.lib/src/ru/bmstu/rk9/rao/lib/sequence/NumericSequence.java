package ru.bmstu.rk9.rao.lib.sequence;

public interface NumericSequence {
	public enum SequenceParametersType {
		DEFINED_PARAMETERS, UNDEFINED_PARAMETERS
	};

	public Double next();
}
