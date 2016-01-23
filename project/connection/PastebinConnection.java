package project.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

//tidy up exceptions and everything else (some code repetition)
public class PastebinConnection {
	private static final URL API_POST;
	private static final URL API_LOGIN;
	private static final URL API_RAW;
	
	static {
		try {
			API_POST = new URL("http://pastebin.com/api/api_post.php");
			API_LOGIN = new URL("http://pastebin.com/api/api_login.php");
			API_RAW = new URL("http://pastebin.com/raw/");
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen unless the URL is incorrect (meaning the programmer made a mistake).", e);
		}
	}
	
	private final String devKey;
	private volatile String userKey = null;
	private volatile boolean loggedIn = false;
	private volatile String accountName = null;
	
	public PastebinConnection(String devKey) {
		if(devKey == null) {
			throw new NullPointerException("Developer key must not be null.");
		}
		
		if(devKey.length() != 32) {	//the length for pastebin developer keys
			throw new IllegalArgumentException("Incorrect length for developer key: Expected 32, got " + devKey.length() + ".");
		}
		
		this.devKey = devKey;
	}

	public String getDevKey() {
		return devKey;
	}
	
	public String getUserKey() {
		return userKey;
	}
	
	public String getAccountName() {
		return loggedIn ? accountName : "Not logged in to any account.";
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public URL paste(Paste paste) throws UnresolvableIOException, BadAPIRequestException {
		synchronized(paste) {
			paste.devKeyLock.lock();
			paste.usingUserKeyLock.lock();
			paste.userKeyLock.lock();
			
			try {
				paste.setDevKey(devKey);
				if(paste.isUsingUserKey()) {
					paste.setUserKey(userKey);
				} else {
					paste.setUserKey(null);
				}
				return new URL(sendSimpleRequest(paste, API_POST));
			} catch (MalformedURLException e) {
				throw new RuntimeException("Malformed URLs should already have been handled in sendRequest.", e);
			} finally {
				paste.userKeyLock.unlock();
				paste.usingUserKeyLock.unlock();
				paste.devKeyLock.unlock();
			}
		}	
	}
	
	//these work forever (or just a long time?) user keys
	public synchronized void login(String username, String password) throws UnresolvableIOException, BadAPIRequestException, LoginException {
		if(loggedIn) {
			throw new LoginException("Already logged in into " + accountName + ".");
		}
		
		final PastebinRequest loginCredentials = new PastebinRequest() {
			private static final String USERNAME = "&api_user_name=";
			private static final String PASSWORD = "&api_user_password=";
			
			private final String POST_request;
			
			{			
				if(username == null || username.equals("")) {
					throw new NullPointerException("Username must not be null nor empty.");
				}
			
				if(password == null || password.equals("")) {
					throw new NullPointerException("Password must not be null nor empty.");
				}
				
				try {
					POST_request = DEVKEY + URLEncoder.encode(devKey, "UTF-8") + USERNAME +
							URLEncoder.encode(username, "UTF-8") + PASSWORD + URLEncoder.encode(password, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
				}
			}

			@Override
			public String asPOST() {
				return POST_request;
			}
		};
		userKey = sendSimpleRequest(loginCredentials, API_LOGIN);
		loggedIn = true;
		accountName = username;
	}
	
	public synchronized void logout() throws LoginException {
		if(!loggedIn) {
			throw new LoginException("Not logged into an account.");
		}
		
		loggedIn = false;
		accountName = null;
		userKey = null;
	}
	
	public synchronized void useSpecificUserKey(String userKey) throws LoginException {
		if(loggedIn) {
			throw new LoginException("Already logged in into " + accountName + ".");
		}
		
		if(userKey != null && userKey.length() != 32) {	//the length for pastebin user keys
			throw new IllegalArgumentException("Incorrect length for user key: Expected 32, got " + userKey.length() + ".");
		}
		
		this.userKey = userKey;
		accountName = "Unknown account name (using specific user key \"" + userKey +"\").";
		loggedIn = true;
	}
	
	//results amount [1, 1000]
	public InputStream getUserPastes(int resultsAmount) throws UnresolvableIOException, LoginException {		
		if(!loggedIn) {
			throw new LoginException("Not logged in.");
		}
		
		if(resultsAmount < 1 || resultsAmount > 1000) {
			throw new IllegalArgumentException("Can't request less than 1 result or more than 1000 pastes.");
		}
		
		PastebinRequest request = new PastebinRequest() {
			private static final String RESULTS_AMOUNT = "&api_results_limit=";
			private static final String OPTION = "&api_option=list";
			
			@Override
			public String asPOST() {
				try {
					return DEVKEY + URLEncoder.encode(devKey, "UTF-8") + USERKEY + URLEncoder.encode(userKey, "UTF-8") +
							RESULTS_AMOUNT + URLEncoder.encode(Integer.toString(resultsAmount), "UTF-8") + OPTION;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
				}
			}
		};
		
		return sendComplexRequest(request, API_POST);
	}
	
	public InputStream getUserPastes() throws UnresolvableIOException, LoginException {
		return getUserPastes(50);	//50 being the default amount of pastes returned by the api, but this way we can provide the same functionality while reusing code
	}
	
	public InputStream getTrendingPastes() throws UnresolvableIOException {
		if(!loggedIn) {
			throw new LoginException("Not logged in.");
		}
		
		final PastebinRequest request = new PastebinRequest() {
			private static final String OPTION = "&api_option=trends";
			
			@Override
			public String asPOST() {
				try {
					return DEVKEY + URLEncoder.encode(devKey, "UTF-8") + OPTION;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
				}
			}
		};
		
		return sendComplexRequest(request, API_POST);
	}

	public String delete(URL pasteURL) throws UnresolvableIOException, BadAPIRequestException {
		if(pasteURL == null) {
			throw new BadAPIRequestException("URL is null.");
		}
		
		final String host = pasteURL.getHost();
		
		if(!pasteURL.getProtocol().equals("http") || !host.contains("pastebin.com")) {
			throw new BadAPIRequestException("URL is not a pastebin url.");
		}
		
		final String path = pasteURL.getPath();
		
		if(path.equals("")) {
			throw new BadAPIRequestException("Path is empty.");
		}
		
		return delete(path.substring(1));	//to remove the /
	}
	
	public String delete(String pasteKey) throws UnresolvableIOException, BadAPIRequestException {
		if(!loggedIn) {
			throw new LoginException("Not logged in.");
		}
		
		if(pasteKey == null || pasteKey.equals("")) {
			throw new IllegalArgumentException("Paste key must not be null nor empty.");
		}
		
		final PastebinRequest request = new PastebinRequest() {
			private static final String PASTEKEY = "&api_paste_key=";
			private static final String OPTION = "&api_option=delete";
			
			@Override
			public String asPOST() {
				try {
					return DEVKEY + URLEncoder.encode(devKey, "UTF-8") + USERKEY + URLEncoder.encode(userKey, "UTF-8")
					+ PASTEKEY + URLEncoder.encode(pasteKey, "UTF-8") + OPTION;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
				}
			}
		};
		
		return sendSimpleRequest(request, API_POST);
	}
	
	public InputStream getUserInfo() throws UnresolvableIOException {
		if(!loggedIn) {
			throw new LoginException("Not logged in.");
		}
		
		final PastebinRequest request = new PastebinRequest() {
			private static final String DEVKEY = "api_dev_key=";
			private static final String USERKEY = "&api_user_key=";
			private static final String OPTION = "&api_option=userdetails";
			
			@Override
			public String asPOST() {
				try {
					return DEVKEY + URLEncoder.encode(devKey, "UTF-8") + USERKEY + URLEncoder.encode(userKey, "UTF-8") + OPTION;
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("This should not happen unless the encoding is incorrect (meaning the programmer made a mistake).", e);
				}
			}
		};
		
		return sendComplexRequest(request, API_POST);
	}
	
	//may return DOCTYPE HTML etc on spam detected
	public static InputStream getPaste(URL pasteURL) throws UnresolvableIOException, BadAPIRequestException {
		if(pasteURL == null) {
			throw new BadAPIRequestException("URL is null.");
		}
		
		final String host = pasteURL.getHost();
		
		if(!pasteURL.getProtocol().equals("http") || !host.contains("pastebin.com")) {
			throw new BadAPIRequestException("URL is not a pastebin url.");
		}
		
		final String path = pasteURL.getPath();
		
		if(path.equals("") || !path.contains("raw")) {
			throw new BadAPIRequestException("URL is not correct.");
		}
		
		if(path.substring(5).equals("")) {
			throw new BadAPIRequestException("Paste key is empty");
		}
		
		try {
			HttpURLConnection connection = (HttpURLConnection) pasteURL.openConnection();
			connection.setReadTimeout(10000);
			connection.setUseCaches(false);
			connection.connect();
			
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new BadAPIRequestException("Server responded with HTTP code " + connection.getResponseCode() + " and message \"" + connection.getResponseMessage() + "\".");
			}
			
			return connection.getInputStream();
		} catch(IOException e) {
			throw new UnresolvableIOException("There's probably not much that can be done to fix this exception on your end. You may not be connected to the internet, or Pastebin may be down.", e);
		}
	}
	
	//this is ugly
	public static URL pastebinURLtoRAW(URL pasteURL) throws BadAPIRequestException {
		if(pasteURL == null) {
			throw new BadAPIRequestException("URL is null.");
		}
		
		final String host = pasteURL.getHost();
		
		if(!pasteURL.getProtocol().equals("http") || !host.contains("pastebin.com")) {
			throw new BadAPIRequestException("URL is not a pastebin url.");
		}
		
		final String path = pasteURL.getPath();
		
		if(path.equals("")) {
			throw new BadAPIRequestException("Path is empty.");
		}
		
		final String pasteKey = path.substring(1);
		
		if(pasteKey.equals("")) {
			throw new BadAPIRequestException("Paste key must not be empty.");
		}
		
		try {
			return new URL(API_RAW.toString() + pasteKey);
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen unless the URL is incorrect (meaning the programmer made a mistake).", e);
		}
	}
	
	public static InputStream getPaste(String pasteKey) throws UnresolvableIOException, BadAPIRequestException {
		if(pasteKey == null || pasteKey.equals("")) {
			throw new BadAPIRequestException("Paste key must not be null nor empty.");
		}
		
		try {
			return getPaste(new URL(API_RAW.toString() + pasteKey));
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should not happen unless the URL is incorrect (meaning the programmer made a mistake).", e);
		}
	}
	
	//does not check whether the returned inputstream has errors or not (bad api request exception)
	//correct encoding: "UTF-8" (same for all other inputstream-returning methods)
	private static InputStream sendComplexRequest(PastebinRequest request, URL url) throws UnresolvableIOException {
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			connection.setUseCaches(false);
			connection.connect();

			try(OutputStream output = connection.getOutputStream();
				Writer writer = new PrintWriter(output);) {
				writer.write(request.asPOST());
				writer.flush();
			} catch (IOException e) {
				throw new IOException("Failed at sending the POST request.", e);
			}
			
			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new BadAPIRequestException("Server responded with HTTP code " + connection.getResponseCode() + " and message \"" + connection.getResponseMessage() + "\".");
			}
			
			return connection.getInputStream();
		} catch(IOException e) {
			throw new UnresolvableIOException("There's probably not much that can be done to fix this exception on your end. You may not be connected to the internet, or Pastebin may be down.", e);
		}
	}
	
	private static String sendSimpleRequest(PastebinRequest request, URL url) throws UnresolvableIOException {
		try {
			final String serverReply;
			
			try(InputStream input = sendComplexRequest(request, url);
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
				serverReply = reader.readLine();
			} catch (IOException e) {
				throw new IOException("Failed at reading the server's reply.", e);
			}
			
			if(serverReply.startsWith("Bad API request")) {
				throw new BadAPIRequestException(serverReply);
			}
			
			return serverReply;
		} catch (ProtocolException e) {
			throw new RuntimeException("This should not happen unless the HTTP Request method is incorrect (meaning the programmer made a mistake).", e);
		} catch (IOException e) {
			throw new UnresolvableIOException("There's probably not much that can be done to fix this exception on your end. You may not be connected to the internet, or Pastebin may be down.", e);
		}
	}
	
}
