package org.cubictest.model.popup;


public class CancelButton extends JavaScriptButton {

	@Override
	public String getType() {
		return "Cancel button";
	}
	
	@Override
	public String getText() {
		return "Cancel";
	}

}