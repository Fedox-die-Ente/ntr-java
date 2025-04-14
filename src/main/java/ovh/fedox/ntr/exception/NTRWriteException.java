package ovh.fedox.ntr.exception;


/**
 * Exception thrown when there is an error writing an NTR file.
 */
public class NTRWriteException extends Exception {

	/**
	 * Creates a new NTR write exception.
	 *
	 * @param message The error message
	 */
	public NTRWriteException(String message) {
		super(message);
	}

	/**
	 * Creates a new NTR write exception.
	 *
	 * @param message The error message
	 * @param cause The cause of the exception
	 */
	public NTRWriteException(String message, Throwable cause) {
		super(message, cause);
	}
}