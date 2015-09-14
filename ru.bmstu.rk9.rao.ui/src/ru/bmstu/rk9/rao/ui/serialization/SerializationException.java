package ru.bmstu.rk9.rao.ui.serialization;

@SuppressWarnings("serial")
public class SerializationException extends RuntimeException {
	public SerializationException(String exceptionMessage) {
		super(exceptionMessage);
	}
}
