/*******************************************************************************
 * Copyright (c) 2005, 2010 Stein K. Skytteren and Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stein K. Skytteren and Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.exporters.selenium.runner.holders;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.common.utils.Logger;
import org.cubictest.exporters.selenium.common.BrowserType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class CubicTestLocalRunner {

	private Selenium selenium;

	public CubicTestLocalRunner(String seleniumServerHostname, int seleniumServerPort, BrowserType browserType, String initialUrl) {
//		selenium = new DefaultSelenium(seleniumServerHostname, seleniumServerPort, browser, initialUrl);
		DesiredCapabilities capability = null;
		switch (browserType) {
		case FIREFOX:
			capability = DesiredCapabilities.firefox();
			break;
		case GOOGLE_CHROME:
			capability = DesiredCapabilities.chrome();
			break;
		case INTERNET_EXPLORER:
			capability = DesiredCapabilities.internetExplorer();
			break;
		case SAFARI:
			capability = DesiredCapabilities.safari();
			break;
		default:
			capability = DesiredCapabilities.htmlUnit();
			break;
		}
		try {
			selenium = new WebDriverBackedSelenium(new RemoteWebDriver(new URL("http://localhost:"+seleniumServerPort+"/wd/hub"), capability),initialUrl);
		} catch (MalformedURLException e) {
			Logger.error(e.getMessage(), e);
		}
	}

	public CubicTestLocalRunner(Selenium selenium) {
		this.selenium = selenium;
	}

	public String execute(String commandName, String locator, String inputValue) throws Throwable {
		try {
			Method method = selenium.getClass().getMethod(commandName, new Class[]{String.class, String.class});
			return method.invoke(selenium, new Object[]{locator, inputValue}) + "";
		} catch (Exception e) {
			throw ErrorHandler.getCause(e);
		}
	}

	public String[] execute(String commandName, String... vars) throws Throwable{
		Class<?>[] classes = new Class[vars.length];
		
		for(int i = 0; i < vars.length; i++){
			classes[i] = vars[i].getClass();
		}
		
		try {
			Method method = selenium.getClass().getMethod(commandName, classes);
			Object result = method.invoke(selenium, (Object[])vars);
			if(result instanceof String[]){
				return (String[]) result;
			}
			return new String[]{result + ""};
		} catch (Exception e) {
			throw ErrorHandler.getCause(e);
		}
	}
	
	public String execute(String commandName, String locator) throws Throwable {
		try {
			Method method = selenium.getClass().getMethod(commandName, new Class[]{String.class});
			return method.invoke(selenium, new Object[]{locator}) + "";
		} catch (Exception e) {
			throw ErrorHandler.getCause(e);
		}
	}

	public String getText(String locator) {
		return selenium.getText(locator);
	}

	public String getTitle() {
		return selenium.getTitle();
	}

	public String getValue(String locator) {
		return selenium.getValue(locator);
	}

	public boolean isTextPresent(String text) {
		return selenium.isTextPresent(text);
	}

	public boolean isElementPresent(String locator) {
		return selenium.isElementPresent(locator);
	}

	public void waitForPageToLoad(String string) {
		selenium.waitForPageToLoad(string);
	}

	public void open(String beginAt) {
		selenium.open(beginAt);
	}
	
	public void open(String beginAt, String ignoreResponseCode) {
		selenium.open(beginAt, ignoreResponseCode);
	}
	
	public void selectFrame(String locator) {
		selenium.selectFrame(locator);
	}

	public void setTimeout(String string) {
		selenium.setTimeout(string);
	}

	public void start() {
		selenium.start();		
	}

	public void stop() {
		selenium.stop();
	}

	public Selenium getSelenium() {
		return selenium;
	}
}
