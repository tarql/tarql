package org.deri.tarql;

public class TarqlException extends RuntimeException {

	public TarqlException(String message) {
		super(message);
	}

	public TarqlException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	public TarqlException(String message, Throwable cause) {
		super(message, cause);
	}
}
