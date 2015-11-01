package rdo.game5;

@SuppressWarnings("serial")
public class Game5Exception extends RuntimeException {
	public Game5Exception(String exceptionMessage) {
		super(exceptionMessage);
	}

	public Game5Exception(Throwable cause) {
		super(cause);
	}
}
