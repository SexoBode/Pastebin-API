package connection;

public class LoginException extends RuntimeException {
	private static final long serialVersionUID = -2034128223720508245L;
	
	public LoginException() {
		super();
	}
	
	public LoginException(String message) {
		super(message);
	}
	
	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}
	
 	protected LoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
 		super(message, cause, enableSuppression, writableStackTrace);
 	}
	
	public LoginException(Throwable cause) {
		super(cause);
	}

}
