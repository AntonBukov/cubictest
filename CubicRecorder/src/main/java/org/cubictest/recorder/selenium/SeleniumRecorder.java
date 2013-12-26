/*******************************************************************************
 * Copyright (c) 2005, 2010 Erlend S. Halvorsen and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Erlend S. Halvorsen - initial API and implementation
 *    Christian Schwarz - enhanced features, bug fixes and usability improvements
 *******************************************************************************/
package org.cubictest.recorder.selenium;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;

import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.common.utils.Logger;
import org.cubictest.common.utils.ModelUtil;
import org.cubictest.common.utils.UserInfo;
import org.cubictest.export.ICubicTestRunnable;
import org.cubictest.export.utils.exported.ExportUtils;
import org.cubictest.exporters.selenium.common.BrowserType;
import org.cubictest.exporters.selenium.launch.LaunchTestRunner;
import org.cubictest.exporters.selenium.launch.RunnerParameters;
import org.cubictest.model.AbstractPage;
import org.cubictest.model.ActionType;
import org.cubictest.model.Page;
import org.cubictest.model.SimpleTransition;
import org.cubictest.model.TransitionNode;
import org.cubictest.recorder.IRecorder;
import org.cubictest.recorder.JSONElementConverter;
import org.cubictest.recorder.JSONRecorder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.json.JSONObject;
import org.openqa.jetty.http.HttpContext;
import org.openqa.jetty.jetty.Server;
import org.openqa.jetty.jetty.servlet.ServletHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

import com.metaparadigm.jsonrpc.JSONRPCServlet;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public class SeleniumRecorder implements ICubicTestRunnable {
	
	private boolean seleniumStarted;
	private Selenium selenium;
	BrowserType browser;
	private SeleniumServer seleniumServer;
	private int port = -1;
	private final String url;
	private final Display display;
	private final LaunchTestRunner initialTestRunner;
	private final IRecorder recorder;

	public SeleniumRecorder(IRecorder recorder, RunnerParameters parameters, BrowserType browser, LaunchTestRunner initialTestRunner) {
		parameters.test.refreshAndVerifySubFiles();
		this.recorder = recorder;
		this.url = ExportUtils.getInitialUrlStartPoint(parameters.test).getBeginAt();
		this.display = parameters.display;
		this.browser = browser;
		this.initialTestRunner = initialTestRunner;
		
		// start server
		
		try {
			port = ExportUtils.findAvailablePort();
			RemoteControlConfiguration config = new RemoteControlConfiguration();
			config.setSingleWindow(false);
			config.setPort(port);
			config.setPortDriversShouldContact(port);
			/*File profileLocation = new File("c:\\workspace_\\profile_15");
			config.setProfilesLocation(profileLocation);
			config.setFirefoxProfileTemplate(profileLocation);*/
			seleniumServer = new SeleniumServer(false, config);
		} catch (Exception e) {
			ErrorHandler.logAndShowErrorDialogAndRethrow("Error starting the recorder.", e);
		}
	}
	
	private String getBaseUrl(String u) {
		if (u.indexOf(".") < 0) {
			//url is already relative
			return u;
		}
		return u.substring(0, u.lastIndexOf("/") + 1);
	}

	class RestoreTread extends Thread{
		boolean runing = false;
		JavascriptExecutor driver;
		JSONRecorder jsonRecorder;
		RestoreTread(JavascriptExecutor driver, JSONRecorder jsonRecorder){
			this.driver = driver;
			this.jsonRecorder = jsonRecorder;
		}
		@Override
		public void run() {
			runing = true;
			while(runing){
				try {
					if((Boolean)driver.executeScript("return typeof window.len_visual_search_injected === 'undefined'")){
						driver.executeScript(readFileAsString("http://localhost:"+port+"/selenium-server/core/scripts/ElementSearch.js"));
					}
					if((Boolean)driver.executeScript("return typeof window.lenpr_command !== 'undefined'")){
						String json = (String)driver.executeScript("return window.lenpr_command");
						driver.executeScript("window.lenpr_command=undefined");
						JSONObject jsonObj = new JSONObject(json);
						String command = jsonObj.getString("command");
						if("Click".equals(command)){
							WebElement element = ((WebDriver)driver).findElement(By.xpath(jsonObj.getString("elementXPath")));
							if(element!=null){
								Actions action = new Actions((WebDriver) driver);
								action.click(element);
								action.perform();
							}
						}else if("Double click".equals(command)){
							WebElement element = ((WebDriver)driver).findElement(By.xpath(jsonObj.getString("elementXPath")));
							if(element!=null){
								Actions action = new Actions((WebDriver) driver);
								action.doubleClick(element);
								action.perform();
							}
						}else if("Enter text".equals(command)){
							WebElement element = ((WebDriver)driver).findElement(By.xpath(jsonObj.getString("elementXPath")));
							if(element!=null){
								Actions action = new Actions((WebDriver) driver);
								action.sendKeys(element, jsonObj.getString("text"));
								action.perform();
							}
							jsonRecorder.addAction(command, jsonObj.getString("object"),jsonObj.getString("text"));
							continue;
						}else if("Text".equals(command)){
							jsonRecorder.assertTextPresent(jsonObj.getString("text"), new JSONObject(jsonObj.getString("object")).getJSONObject("properties").getString("cubicId"));
							continue;
						}else if("Title".equals(command)){
							jsonRecorder.setStateTitle(jsonObj.getString("title"));
							jsonRecorder.assertPresent(jsonObj.getString("object"));
							continue;
						}else if("Present".equals(command)){
							jsonRecorder.assertPresent(jsonObj.getString("object"));
							continue;
						}else if("Not present".equals(command)){
							jsonRecorder.assertNotPresent(jsonObj.getString("object"));
							continue;
						}
						jsonRecorder.addAction(command, jsonObj.getString("object"));
					}
					Thread.sleep(500);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					try {
						if (seleniumServer != null)
							seleniumServer.stop();
					} catch(SeleniumException e1) {
						Logger.error(e.getMessage(), e1);
					}
					return;
				}
			}
		}
	}
	RestoreTread restore;
	public void stop() {
		restore.runing = false;
		try {
			if (seleniumServer != null)
				seleniumServer.stop();
		} catch(SeleniumException e) {
			Logger.error(e.getMessage(), e);
		}
	}

	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
			seleniumServer.start();
			DesiredCapabilities capability = DesiredCapabilities.firefox();
			WebDriver driver = new RemoteWebDriver(new URL("http://localhost:"+port+"/wd/hub"), capability);
			selenium = new WebDriverBackedSelenium(driver,url);
			selenium.open(url, "true");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				ErrorHandler.logAndShowErrorDialogAndRethrow(e);
			}
			String baseUrl = getBaseUrl(url);
			restore = new RestoreTread((JavascriptExecutor) driver,new JSONRecorder(recorder, new JSONElementConverter(baseUrl)));
			restore.start();
			seleniumStarted = true;
        } catch (final Exception e) {
        	String msg = "";
        	if (e.toString().indexOf("Location.href") >= 0 || e.toString().indexOf("Security error") >= 0) {
        		msg += "Looks like Selenium failed when following a redirect. If this occured at start of test, " +
        				"try modifying the start point URL to the correct/redirected address.\n\n";
        	}
        	msg += "Error occured when recording test. Recording might not work.";
        	final String finalMsg = msg;
    		display.syncExec(new Runnable() {
    			public void run() {
    				UserInfo.showErrorDialog(e, finalMsg);
    			}
    		});
			ErrorHandler.logAndRethrow(finalMsg, e);
		}
        
        if (initialTestRunner != null) {
			recorder.setEnabled(false);
        	initialTestRunner.setSelenium(selenium);
        	TransitionNode lastNodeInTestOnFirstPath = ModelUtil.getLastNodeInGraph(initialTestRunner.getTest().getStartPoint());
        	initialTestRunner.setTargetPage(lastNodeInTestOnFirstPath);
        	initialTestRunner.run(monitor);
			
        	if (!(lastNodeInTestOnFirstPath instanceof Page) || ((Page) lastNodeInTestOnFirstPath).hasElements()) {
        		//create new page for start of recording
        		Page newPage = new Page();
        		newPage.setName("Record start");
        		SimpleTransition transition = new SimpleTransition();
        		transition.setStart(lastNodeInTestOnFirstPath);
        		transition.setEnd(newPage);
        		recorder.addToTest(transition, newPage);
        		lastNodeInTestOnFirstPath = newPage;
        		
        	}
        	recorder.setCursor((AbstractPage) lastNodeInTestOnFirstPath);
        }
        recorder.setEnabled(true);
	}
	
	private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        URLConnection con = new URL(filePath).openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(),"utf-8"));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

	public Selenium getSelenium() {
		return selenium;
	}

	public boolean isSeleniumStarted() {
		return seleniumStarted;
	}

	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	public String getResultMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTargetPage(TransitionNode selectedPage) {
		// TODO Auto-generated method stub
		
	}

}
