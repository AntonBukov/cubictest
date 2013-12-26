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
package org.cubictest.recorder;

import java.text.ParseException;

import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.model.ActionType;
import org.cubictest.model.PageElement;
import org.cubictest.model.Text;
import org.cubictest.model.UserInteraction;
import org.json.JSONObject;

import com.metaparadigm.jsonrpc.JSONSerializer;

public class JSONRecorder {
	private final IRecorder recorder;
	private JSONSerializer serializer;
	private final JSONElementConverter converter;

	public JSONRecorder(IRecorder recorder, JSONElementConverter converter) {
		this.recorder = recorder;
		this.converter = converter;

		serializer = new JSONSerializer();
		try {
			serializer.registerDefaultSerializers();
		} catch (Exception e) {
			ErrorHandler.logAndShowErrorDialogAndRethrow(e);
		}
	}

	public boolean assertPresent(String json) {
		if (!recorder.isEnabled())
			return false;

		try {
			JSONObject jsonObj = new JSONObject(json);
			String contextCubicId = jsonObj.getString("parentCubicId");

			PageElement parent = converter.getPageElement(contextCubicId);
			PageElement pe = converter.createElementFromJson(jsonObj);
			if (pe != null) {
				recorder.addPageElement(pe, parent);
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			ErrorHandler.logAndShowErrorDialogAndRethrow(e);
		}

		return false;
	}

	public boolean assertNotPresent(String json) {
		if (!recorder.isEnabled())
			return false;

		try {
			JSONObject jsonObj = new JSONObject(json);
			String contextCubicId = jsonObj.getString("parentCubicId");

			PageElement pe = converter.createElementFromJson(jsonObj);
			PageElement parent = converter.getPageElement(contextCubicId);
			pe.setNot(true);
			recorder.addPageElement(pe, parent);
			return true;
		} catch (ParseException e) {
			ErrorHandler.logAndShowErrorDialogAndRethrow(e);
		}
		return false;
	}

	public boolean assertTextPresent(String text, String contextCubicId) {
		if (!recorder.isEnabled())
			return false;

		PageElement pe = new Text();
		pe.setText(text);
		PageElement parent = converter.getPageElement(contextCubicId);
		recorder.addPageElement(pe, parent);
		return true;
	}

	public void addAction(String actionType, String jsonElement) {
		if (!recorder.isEnabled())
			return;

		this.addAction(actionType, jsonElement, "");
	}

	public void addAction(String actionType, String jsonElement, String value) {
		if (!recorder.isEnabled())
			return;

		try {
			JSONObject jsonObj = new JSONObject(jsonElement);
			PageElement pe = converter.createElementFromJson(jsonObj);

			if (pe != null) {
				UserInteraction action = new UserInteraction(pe,
						ActionType.getActionType(actionType), value);
				String contextCubicId = jsonObj.getString("parentCubicId");
				PageElement parent = converter.getPageElement(contextCubicId);
				recorder.addUserInput(action, parent);
			} else {
				System.out.println("Action ignored");
			}
		} catch (Exception e) {
			ErrorHandler.logAndRethrow(e);
		}
	}

	public void setStateTitle(String title) {
		if (!recorder.isEnabled())
			return;
		recorder.setStateTitle(title);
	}

/*	public String getViews() {
		JSONArray views = new JSONArray();
		IFile file = recorder.getCurrentProject().getFile("locators.xml");
		if(!file.exists()||file.isReadOnly())
			return views.toString();
		try {
			views =(JSONArray)new XStream().fromXML(new FileInputStream(file.getLocation().toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject menu = new JSONObject();
		JSONArray items = new JSONArray();
		JSONObject item = new JSONObject();
		menu.put("xpath", "//select");
		item.put("label", "Assert Option Present");
		item.put(
				"action",
				"function(e) {"
						+ "var opt = element.options[element.selectedIndex];"
						+ "var parentCubicId = element.cubicId; "
						+ "if(typeof parentCubicId == 'undefined'){"
						+ "menu.rpcRecorder.assertPresent(Cubic.dom.serializeDomNode(element));"
						+ "parentCubicId = element.cubicId;} "
						+ "if(opt != null && typeof opt != 'undefined'){"
						+ "menu.rpcRecorder.assertPresent(Cubic.dom.serializeDomNode(opt));"
						+ "}}");
		items.put(item);
		item = new JSONObject();
		item.put("label", "Assert All Options Present");
		item.put(
				"action",
				"function(e) {"
						+ "var parentCubicId = element.cubicId;"
						+ "if(typeof parentCubicId == 'undefined'){"
						+ "menu.rpcRecorder.assertPresent(Cubic.dom.serializeDomNode(element));"
						+ "parentCubicId = element.cubicId;}"
						+ "for(var i = 0; i < element.options.length; i++){"
						+ "var opt = element.options[i];"
						+ "if(opt != null && typeof opt != 'undefined'){"
						+ "menu.rpcRecorder.assertPresent(Cubic.dom.serializeDomNode(opt));"
						+ "}}}");
		items.put(item);
		menu.put("menu", items);
		views.put(menu);
		views.put(JSONMenuUtil.getStndartMenu("IMG"));
		views.put(JSONMenuUtil.getStndartMenu("INPUT"));
		views.put(JSONMenuUtil.getStndartMenu("A"));
		views.put(JSONMenuUtil.getStndartMenu("TEXTAREA"));
		views.put(JSONMenuUtil.getStndartMenu("SELECT"));
		views.put(JSONMenuUtil.getStndartMenu("BUTTON"));
		views.put(JSONMenuUtil.getStndartMenu("DIV"));
		views.put(JSONMenuUtil.getStndartMenu("TABLE"));
		try {
			new XStream().toXML(views,new FileOutputStream(file.getLocation().toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return views.toString();
	}*/

}