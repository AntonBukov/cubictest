/*
 * Created on Apr 27, 2005
 * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
 * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
*/
package org.cubictest.exporters.watir.converters;

import static org.cubictest.model.ActionType.GO_BACK;
import static org.cubictest.model.ActionType.GO_FORWARD;
import static org.cubictest.model.ActionType.NEXT_WINDOW;
import static org.cubictest.model.ActionType.PREVIOUS_WINDOW;
import static org.cubictest.model.ActionType.REFRESH;
import static org.cubictest.model.ActionType.SELECT;
import static org.cubictest.model.IdentifierType.LABEL;

import java.util.Iterator;
import java.util.List;

import org.cubictest.common.utils.Logger;
import org.cubictest.export.converters.ITransitionConverter;
import org.cubictest.export.exceptions.ExporterException;
import org.cubictest.exporters.watir.holders.RubyBuffer;
import org.cubictest.exporters.watir.holders.StepList;
import org.cubictest.exporters.watir.utils.WatirUtils;
import org.cubictest.model.ActionType;
import org.cubictest.model.FormElement;
import org.cubictest.model.IActionElement;
import org.cubictest.model.IdentifierType;
import org.cubictest.model.PageElement;
import org.cubictest.model.UserInteraction;
import org.cubictest.model.UserInteractionsTransition;
import org.cubictest.model.WebBrowser;
import org.cubictest.model.formElement.Button;
import org.cubictest.model.formElement.Option;
import org.cubictest.model.formElement.Select;

/**
 * Converts transitions to watir test code.
 * 
 * @author chr_schwarz
 */
public class TransitionConverter implements ITransitionConverter<StepList> {
	
	
	/**
	 * Converts a user interactions transition to a list of Watir steps.
	 * 
	 * @param transition The transition to convert.
	 */
	public void handleUserInteractions(StepList stepList, UserInteractionsTransition transition) {
		List actions = transition.getUserInteractions();
		Iterator it = actions.iterator();
		while(it.hasNext()) {
			UserInteraction action = (UserInteraction) it.next();
			IActionElement actionElement = action.getElement();
			
			if (actionElement == null) {
				Logger.warn("Action element was null. Skipping user interaction: " + transition);
				continue;
			}
			stepList.addSeparator();
			
			if (actionElement instanceof PageElement) {
				handlePageElementAction(stepList, action);
			}
			else if (actionElement instanceof WebBrowser) {
				handleWebBrowserAction(stepList, action);
			}
		}
	}
	
	
	/**
	 * Converts a UserInteraction on a page element to a Watir Step.
	 */
	private void handlePageElementAction(StepList stepList, UserInteraction userInteraction) {
		PageElement pe = (PageElement) userInteraction.getElement();
		String idType = WatirUtils.getIdType(pe);
		String idText = "\"" + pe.getText() + "\"";

		//Handle Label identifier:		
		if (WatirUtils.shouldExamineHtmlLabelTag(pe)) {
			stepList.add(WatirUtils.getLabelTargetId(pe));
			stepList.addSeparator();
			idText = "labelTargetId";
			idType = ":id";
		}
		
		stepList.add("# user interaction");
		if (userInteraction.getActionType().equals(SELECT)) {
			//select option in select list:
			selectOptionInSelectList(stepList, (Option) pe, idType, idText);	
		}
		else {
			//handle all other interaction types:
			int indent = 2;
			if (WatirUtils.shouldExamineHtmlLabelTag(pe)) {
				stepList.add("if (labelTargetId == nil)", 2);
				stepList.add("puts \"Could not " + WatirUtils.getInteraction(userInteraction) + " " + 
						WatirUtils.getElementType(pe) + " with " + pe.getIdentifierType() + " = '" + pe.getText() +
						"' (element not found in page)", 3);
				stepList.add("else", 2);
				indent = 3;
			}
			
			//the action:
			stepList.add("ie." + WatirUtils.getElementType(pe) + "(" + idType + ", " + idText + ")." + WatirUtils.getInteraction(userInteraction), indent);

			if (WatirUtils.shouldExamineHtmlLabelTag(pe)) {
				stepList.add("end", 2);
			}
		}
	}

	
	
	/**
	 * Selects the specified option in a select list.
	 */
	private void selectOptionInSelectList(StepList stepList, Option option, String idType, String idText) {
		Select select = (Select) option.getParent();
		String selectIdText = "\"" + select.getText() + "\"";
		String selectIdType = WatirUtils.getIdType(select);
		
		if (select.getIdentifierType().equals(IdentifierType.LABEL)) {
			//Handle label:
			stepList.add(WatirUtils.getLabelTargetId(select));
			stepList.addSeparator();
			selectIdText = "labelTargetId";
			selectIdType = ":id";
		}
		
		String selectList = "ie.select_list(" + selectIdType + ", " + selectIdText + ")";
		
		//Select the option:
		if (option.getIdentifierType().equals(LABEL)) {
			stepList.add(selectList + ".select(" + idText + ")");
		}
		else {
			stepList.add(selectList + ".option(" + idType + ", " + idText + ").select");
		}
	}
	
	
	
	/**
	 * Converts a Web browser user interaction to a Watir step.
	 */
	private void handleWebBrowserAction(StepList steps, UserInteraction userInteraction) {

		ActionType actionType = userInteraction.getActionType();
			
		if (actionType.equals(GO_BACK)) {
			steps.add("ie.back()");
		}
		else if (actionType.equals(GO_FORWARD)) {
			steps.add("ie.forward()");
		}
		else if (actionType.equals(REFRESH)){
			steps.add("ie.refresh()");
		}
		else if (actionType.equals(NEXT_WINDOW)){
			// TODO: should call the IE.attach method in Watir, it requires either an URL or the name of the window.
			// probably best to just use the name, in order to make it work for more frameworks
			throw new ExporterException("Previous window not supported by Watir");
		}
		else if (actionType.equals(PREVIOUS_WINDOW)) {
			// TODO:  should call the IE.attach method in Watir, it requires either an URL or the name of the window.
			// probably best to just use the name, in order to make it work for more frameworks
			throw new ExporterException("Previous window not supported by Watir");
		}
	}
}