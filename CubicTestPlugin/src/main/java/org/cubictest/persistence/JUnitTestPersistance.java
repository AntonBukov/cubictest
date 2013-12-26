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
package org.cubictest.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.cubictest.common.exception.TestNotFoundException;
import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.common.utils.Logger;
import org.cubictest.model.Test;

import com.thoughtworks.xstream.converters.ConversionException;


/**
 * Class responsible for writing a test to file and loading a test from file. 
 * 
 * @author chr_schwarz
 */
public class JUnitTestPersistance {
	

	/**
	 * Writes a test to the given file.
	 * 
	 * @param test The test to save.
	 * @param file The file to save to.
	 */
	public static void saveToFile(Test test, File file) {
		String xml = new CubicTestXStream().toXML(test);
		try {
			String charset = getCharset(file);
			String charsetHeader = getCharsetHeader(charset);
			xml = charsetHeader + "\n" + xml;
			FileUtils.writeStringToFile(file, xml, charset);
		} catch (IOException e) {
			ErrorHandler.logAndRethrow(e);
		}
	}

	
	public static String getCharsetHeader(String charset) {
		return "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>";
	}


	/**
	 * Reads a test from File, upgrading legacy tests if necessary.
	 * 
	 * @param file The file containing the test. 
	 * @return The test.
	 */
	public static Test loadFromFile(File file) {
		String xml = "";
		try {
			String charset = getCharset(file);
			xml = FileUtils.readFileToString(file, charset);
		} catch (FileNotFoundException e) {
			Logger.error("Test file not found.", e);
			throw new TestNotFoundException(e.getMessage());
		} catch (IOException e) {
			ErrorHandler.logAndRethrow(e);
		}
		return loadFromString(xml,file.getName());
	}

	public static Test loadFromString(String xml, String fileName) {
		Test test = null;
		try {
			test = (Test) new CubicTestXStream().fromXML(xml);
			test.getAllLanguages().updateAllLanguages();
			if (test.getParamList() != null) {
				test.setParamList(test.getParamList().getNewUpdatedVersion());
			}
		} catch (Exception e) {
			if (ErrorHandler.getCause(e) instanceof ConversionException) {
				ErrorHandler.logAndShowErrorDialogAndRethrow("Could not load test (error creating Test from XML file \"" + fileName + "\"). If the test was created with a newer version of Lenny, then please upgrade to that version.\n", e);
			}
			else {
				ErrorHandler.logAndShowErrorDialogAndRethrow("Exception occured. Could not load test \"" + fileName + "\"", e);
			}
		}
		
		return test;
	}
	
	public static String getCharset(File file) {
		String charset = null;
		try{
				String test = FileUtils.readFileToString(file);
				if(test.startsWith("<?xml")){
					int start = test.indexOf("encoding=\"") + 10;
					int end = test.indexOf("\"?>",start);
					String encoding = test.substring(start, end);
					if(CharEncoding.isSupported(encoding))
						return encoding;
				}
			}catch(IOException e2){
			}
		if(charset == null)
			charset = "ISO-8859-1";
		return charset;
	}

}
