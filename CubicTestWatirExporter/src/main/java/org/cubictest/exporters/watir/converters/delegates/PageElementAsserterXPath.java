/*******************************************************************************
 * Copyright (c) 2005, 2010  Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.exporters.watir.converters.delegates;

import static org.cubictest.exporters.watir.utils.WatirUtils.escape;

import org.cubictest.export.exceptions.ExporterException;
import org.cubictest.exporters.watir.holders.WatirHolder;
import org.cubictest.exporters.watir.utils.WatirUtils;
import org.cubictest.model.PageElement;
import org.cubictest.model.Text;
import org.cubictest.model.Title;
import org.cubictest.model.formElement.Option;

/**
 * Page element converter that uses XPath.
 * 
 * @author Christian Schwarz
 */
public class PageElementAsserterXPath {

	
	public static void handle(WatirHolder watirHolder, PageElement pe) {
		if (pe instanceof Title || pe instanceof Option) {
			throw new ExporterException("Internal error. " + pe.getType() + " not supported by this asserter.");
		}
		else {
			//handle all page elements:			
			watirHolder.add("pass = 0", 3);
			String xpath = escape(watirHolder.getFullContextWithAllElements(pe));
			String container = watirHolder.getActiveContainer();
	
			if (pe instanceof Text) {
				//use element_by_xpath
				watirHolder.add(watirHolder.getVariableName(pe) + " = " + container + ".element_by_xpath(\"" + xpath + "\")", 3);
				String not = pe.isNot() ? "" : "not "; 
				watirHolder.add("while " + not + watirHolder.getVariableName(pe) + ".methods.member?(\"ole_get_methods\") do", 3);
			}
			else {
				//others: use :xpath locator type in specific element class
				watirHolder.add(watirHolder.getVariableName(pe) + " = " + container + "." + WatirUtils.getElementType(pe) + "(:xpath, \"" + xpath + "\")", 3);
				String not = pe.isNot() ? "": "not "; 
				watirHolder.add("while " + not + watirHolder.getVariableName(pe) + ".exists? do", 3);
			}
			watirHolder.add("if (pass > " + (watirHolder.getNextPageElementTimeout() * 10) + ")", 4);
			watirHolder.add("raise " + WatirHolder.TEST_STEP_FAILED, 5);
			watirHolder.add("end", 4);
			watirHolder.add("sleep 0.1", 4);
			watirHolder.add("pass += 1", 4);
			watirHolder.add("end", 3);
	
		}
	}
	
}
