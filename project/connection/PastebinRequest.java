package project.connection;

abstract class PastebinRequest {
	static final String DEVKEY = "api_dev_key=";
	static final String USERKEY = "&api_user_key=";

	abstract String asPOST();
}
