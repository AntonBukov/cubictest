/*******************************************************************************
 * Copyright (c) 2005, 2008 Stein K. Skytteren and Christian Schwarz
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Stein K. Skytteren and Christian Schwarz - initial API and implementation
 *******************************************************************************/
package org.cubictest.export.holders;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.cubictest.export.utils.exported.XPathBuilder;
import org.cubictest.model.ConnectionPoint;
import org.cubictest.model.PageElement;
import org.cubictest.model.PropertyAwareObject;
import org.cubictest.model.SubTest;
import org.cubictest.model.TransitionNode;
import org.cubictest.model.context.Frame;
import org.cubictest.model.context.IContext;

/**
 * Holder for context info for runners and exporters.
 * 
 * @author Christian Schwarz
 */
public class ContextHolder implements IResultHolder {


	protected Stack<PropertyAwareObject> breadCrumbs = new Stack<PropertyAwareObject>(); 
	private Stack<IContext> contextStack = new Stack<IContext>(); 
	private Stack<Stack<IContext>> frameStack = new Stack<Stack<IContext>>(); 
	private Map<PageElement, PageElement> elementParentMap = new HashMap<PageElement, PageElement>();
	private boolean shouldFailOnAssertionFailure;
	
	/**
	 * Set new context, i.e. XPath starting point (path) for lookup of elements.
	 * @param ctx
	 */
	public void pushContext(IContext ctx) {

		//all interesting contexts are page elements
		if (ctx instanceof PageElement) {
			contextStack.push(ctx);
			
			for (PageElement pe : ctx.getRootElements()) {
				//setting current context as parent of each page element within context
				elementParentMap.put(pe, (PageElement) ctx);
			}
		}
	}

	
	/**
	 * Get the previous context, i.e. the previous XPath starting point (path) for lookup of elements.
	 */
	public void popContext() {
		contextStack.pop();
	}
	

	public String toResultString() {
		//should be subclassed
		return "";
	}
	
	
	public boolean isInRootContext() {
		return contextStack.size() == 0;
	}
	
	public String getFullContextWithoutAllElements(PageElement pageElement){
		return getSmartElement(pageElement,pageElement);
	}

	private String getSmartElement(PageElement pageElement, PageElement orgElement) {
		String res = "";
		if (pageElement == null) {
			return "";
		}
		
		String axis = "/descendant-or-self::";
		if (isInRootContext()) {
			axis = "//";
		}
		res += axis + XPathBuilder.getXPath(pageElement);
		
		PageElement parent = elementParentMap.get(pageElement);
		return getSmartElement(parent, orgElement) + res;

	}


	/**
	 * Gets "smart context": Asserts all elements in context present.
	 */
	public String getFullContextWithAllElements(PageElement pageElement) {
		return getSmartContext(pageElement, pageElement);
	}
	
	/**
	 * Recursive private utility method. Gets "smart context": Asserts all or previous elements in context present.
	 * @param pageElement Current page element to get XPath for.
	 * @param orgElement The original element for the query.
	 */
	private String getSmartContext(PageElement pageElement, PageElement orgElement) {
		String res = "";
		if (pageElement == null) {
			return "";
		}
		
		String axis = "/descendant-or-self::";
		if (isInRootContext()) {
			axis = "//";
		}
		res += axis + XPathBuilder.getXPath(pageElement);
		
		if (pageElement instanceof IContext && ((IContext) pageElement).getRootElements().size() > 1) {
			String assertion = getElementsInContextXPath((IContext) pageElement, orgElement);
			if (StringUtils.isNotBlank(assertion)) {
				res += "[" + assertion + "]";
			}
		}
		
		PageElement parent = elementParentMap.get(pageElement);
		return getSmartContext(parent, orgElement) + res;
	}
	


	private String getElementsInContextXPath(IContext context, PageElement orgElement) {
		String res = "";
		int i = 0;
		for (PageElement pe : context.getRootElements()) {
			if (pe.equals(orgElement)) {
				continue; //skip current element
			}
			if (pe.isNot()) {
				continue; //skip elements that are not there
			}
			if (i > 0) {
				res += "][";
			}
			res += ".//" + XPathBuilder.getXPath(pe);
			i++;
		}
		return res;
	}


	public void updateStatus(SubTest subTest, boolean hadException, ConnectionPoint targetConnectionPoint) {
		//Empty. Can be overridden if exporters want to update sub test statuses.
	}


	public boolean shouldFailOnAssertionFailure() {
		return shouldFailOnAssertionFailure;
	}

	public void setShouldFailOnAssertionFailure(boolean shouldFailOnAssertionFailure) {
		this.shouldFailOnAssertionFailure = shouldFailOnAssertionFailure;
	}
	
	public void popBreadcrumb() {
		breadCrumbs.pop();
	}

	public void pushBreadcrumb(PropertyAwareObject element) {
		breadCrumbs.push(element);
	}

	public String getCurrentBreadcrumbs() {
		String res = "";
		for (PropertyAwareObject obj : breadCrumbs) {
			if (StringUtils.isNotBlank(res)) {
				res += " --> ";
			}
			if (obj instanceof TransitionNode) {
				res += ((TransitionNode) obj).getName();
			}
			else if (obj instanceof PageElement) {
				res += ((PageElement) obj).getDirectEditIdentifier().getValue();
			}
		}
		return res;
	}
	
	public void pushFrame(Frame frame) {
		frameStack.push(contextStack);
		contextStack = new Stack<IContext>();
	}

	public void popFrame() {
		contextStack = frameStack.pop();
	}
}
