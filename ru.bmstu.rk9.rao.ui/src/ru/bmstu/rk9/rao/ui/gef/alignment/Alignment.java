package ru.bmstu.rk9.rao.ui.gef.alignment;

public enum Alignment {

	FIRST_LINE_START("Начало первой строки"), PAGE_START("Начало страницы"), FIRST_LINE_END(
			"Конец первой строки"), LINE_START("Начало строки"), CENTER("По центру"), LINE_END(
					"Конец строки"), LAST_LINE_START("Начало последней строки"), PAGE_END(
							"Конец страницы"), LAST_LINE_END("Конец последней строки");

	private String description;

	private Alignment(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static final Alignment defaultAlignment = FIRST_LINE_START;
	public static final Alignment[] ordered = { FIRST_LINE_START, PAGE_START, FIRST_LINE_END, LINE_START, CENTER,
			LINE_END, LAST_LINE_START, PAGE_END, LAST_LINE_END };

}
