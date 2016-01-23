package connection;

public class UnresolvableIOException extends RuntimeException {
	private static final long serialVersionUID = -5302973863514967711L;

	public UnresolvableIOException() {
		super();
	}
	
	public UnresolvableIOException(String message) {
		super(message);
	}
	
	public UnresolvableIOException(String message, Throwable cause) {
		super(message, cause);
	}
	
 	protected UnresolvableIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
 		super(message, cause, enableSuppression, writableStackTrace);
 	}
	
	public UnresolvableIOException(Throwable cause) {
		super(cause);
	}

}
