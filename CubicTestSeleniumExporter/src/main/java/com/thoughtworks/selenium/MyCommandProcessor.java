package com.thoughtworks.selenium;

public class MyCommandProcessor extends HttpCommandProcessor {

	public MyCommandProcessor(String serverHost, int serverPort,
			String browserStartCommand, String browserURL) {
		super(serverHost, serverPort, browserStartCommand, browserURL);
	}

	public void setSessionId(String sessionId) {
		setSessionInProgress(sessionId);
	}

}
