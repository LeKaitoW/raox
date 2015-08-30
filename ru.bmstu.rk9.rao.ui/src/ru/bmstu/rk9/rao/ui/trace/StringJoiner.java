package ru.bmstu.rk9.rao.ui.trace;

public class StringJoiner {
	private final String delimiter;
	private final String prefix;
	private final String suffix;

	private String current = "";

	public enum StringFormat {
		FUNCTION(", ", "(", ")"), STRUCTURE(", ", "{", "}"), ARRAY(", ", "[",
				"]"), ENUMERATION(", ", "", "");

		StringFormat(String delimiter, String prefix, String suffix) {
			this.delimiter = delimiter;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		private final String delimiter;
		private final String prefix;
		private final String suffix;
	}

	public final String getString() {
		return prefix + current + suffix;
	}

	public StringJoiner(StringFormat format) {
		this.delimiter = format.delimiter;
		this.prefix = format.prefix;
		this.suffix = format.suffix;
	}

	public StringJoiner(String delimiter) {
		this(delimiter, "", "");
	}

	public StringJoiner(String delimiter, String prefix, String postfix) {
		this.delimiter = delimiter;
		this.prefix = prefix;
		this.suffix = postfix;
	}

	public final StringJoiner add(final String toAppend) {
		if (current == "")
			current = new String(toAppend);
		else
			current += delimiter + toAppend;
		return this;
	}

	public final StringJoiner add(final int toAppend) {
		return add(String.valueOf(toAppend));
	}

	public final StringJoiner add(final short toAppend) {
		return add(String.valueOf(toAppend));
	}

	public final StringJoiner add(final double toAppend) {
		return add(String.valueOf(toAppend));
	}

	public final StringJoiner add(final boolean toAppend) {
		return add(String.valueOf(toAppend));
	}
}