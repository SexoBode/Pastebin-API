package project.connection;

public class BadAPIRequestException extends RuntimeException {
	private static final long serialVersionUID = 4925155407045706277L;

	public BadAPIRequestException() {
		super();
	}
	
	public BadAPIRequestException(String message) {
		super(message);
	}
	
	public BadAPIRequestException(String message, Throwable cause) {
		super(message, cause);
	}
	
 	protected BadAPIRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
 		super(message, cause, enableSuppression, writableStackTrace);
 	}
	
	public BadAPIRequestException(Throwable cause) {
		super(cause);
	}
	
}
