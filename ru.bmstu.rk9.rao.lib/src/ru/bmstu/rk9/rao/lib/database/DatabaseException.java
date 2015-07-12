package ru.bmstu.rk9.rao.lib.database;

@SuppressWarnings("serial")
public class DatabaseException extends RuntimeException {
	public DatabaseException(String exceptionMessage) {
		super(exceptionMessage);
	}
}
