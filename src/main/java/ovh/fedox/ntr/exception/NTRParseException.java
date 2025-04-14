package ovh.fedox.ntr.exception;

/**
 * Exception thrown when there is an error parsing an NTR file.
 */
public class NTRParseException extends Exception {

	/**
	 * Creates a new NTR parse exception.
	 *
	 * @param message The error message
	 */
	public NTRParseException(String message) {
		super(message);
	}

	/**
	 * Creates a new NTR parse exception.
	 *
	 * @param message The error message
	 * @param cause The cause of the exception
	 */
	public NTRParseException(String message, Throwable cause) {
		super(message, cause);
	}
}