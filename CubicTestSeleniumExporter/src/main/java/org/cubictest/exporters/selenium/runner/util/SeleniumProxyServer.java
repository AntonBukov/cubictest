/*
 * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
 * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
 */
package org.cubictest.exporters.selenium.runner.util;

import org.cubictest.common.settings.CubicTestProjectSettings;
import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.common.utils.Logger;
import org.cubictest.exporters.selenium.utils.SeleniumUtils;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Selenium proxy server used by the runner.
 * Runs as a thread.
 * 
 * @author Christian Schwarz
 */
public class SeleniumProxyServer {

	SeleniumServer seleniumServer;
	Thread serverThread;
	int port;
	boolean started;
	
	
	public SeleniumProxyServer(CubicTestProjectSettings settings, int port) {
		try {
			this.port = port;
			seleniumServer = new SeleniumServer(port);
			
			Boolean inject = settings.getBoolean(SeleniumUtils.getPluginPropertyPrefix(), "useSeleniumProxyInjectionMode", false);
			if (inject != null && inject) {
				seleniumServer.setProxyInjectionMode(true);
			}
			else {
				seleniumServer.setProxyInjectionMode(false);
			}
			
			final int portInfo = port;
	        serverThread = new Thread(new Runnable() {
	            public void run() {
	                try {
	        			Logger.info("Starting selenium server at port " + portInfo);
	                    seleniumServer.start();
	                    started = true;
	                    Logger.info("Server started");
	                }
	                catch (Exception e) {
	                    ErrorHandler.logAndShowErrorDialogAndRethrow("Error starting selenium server.", e);
	                }
	            }
	        });
	        
		} catch (Exception e) {
            ErrorHandler.logAndShowErrorDialogAndRethrow("Error starting selenium server.", e);
		}
	}
	
	/**
	 * Starts the server.
	 */
	public void start() {
        serverThread.start();
		
	}
	
	/**
	 * Stops the server.
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException{
		Logger.info("Stopping selenium server at port " + port);
		seleniumServer.stop();
	}
	

	public int getPort() {
		return port;
	}

	public boolean isStarted() {
		return started;
	}
}
