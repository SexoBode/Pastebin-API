package project.connection;

public class BadPasteException extends RuntimeException {
	private static final long serialVersionUID = 4924156008745252970L;
	
	public BadPasteException() {
		super();
	}
	
	public BadPasteException(String message) {
		super(message);
	}
	
	public BadPasteException(String message, Throwable cause) {
		super(message, cause);
	}
	
 	protected BadPasteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
 		super(message, cause, enableSuppression, writableStackTrace);
 	}
	
	public BadPasteException(Throwable cause) {
		super(cause);
	}

}
