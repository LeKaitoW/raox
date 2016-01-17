package ru.bmstu.rk9.rao.lib.sequence;

public interface Sequence {
	public enum SequenceParametersType {DEFINED_PARAMETERS, UNDEFINED_PARAMETERS};

	public Double next();
}
