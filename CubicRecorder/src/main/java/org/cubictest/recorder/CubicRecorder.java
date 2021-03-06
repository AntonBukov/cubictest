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

import static org.cubictest.model.ActionType.CLICK;

import org.cubictest.exporters.selenium.common.BrowserType;
import org.cubictest.model.AbstractPage;
import org.cubictest.model.ActionType;
import org.cubictest.model.ExtensionStartPoint;
import org.cubictest.model.IActionElement;
import org.cubictest.model.Image;
import org.cubictest.model.Link;
import org.cubictest.model.Page;
import org.cubictest.model.PageElement;
import org.cubictest.model.Test;
import org.cubictest.model.Transition;
import org.cubictest.model.TransitionNode;
import org.cubictest.model.UrlStartPoint;
import org.cubictest.model.UserInteraction;
import org.cubictest.model.UserInteractionsTransition;
import org.cubictest.model.context.IContext;
import org.cubictest.model.formElement.Button;
import org.cubictest.recorder.launch.SynchronizedCommandStack;
import org.cubictest.ui.gef.command.AddAbstractPageCommand;
import org.cubictest.ui.gef.command.ChangeNameCommand;
import org.cubictest.ui.gef.command.CreatePageElementCommand;
import org.cubictest.ui.gef.command.CreateTransitionCommand;
import org.cubictest.ui.gef.interfaces.exported.ITestEditor;
import org.cubictest.ui.gef.layout.AutoLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Display;

public class CubicRecorder implements IRecorder {
	public static final BrowserType[] SUPPORTED_BROWSERS = new BrowserType[] { BrowserType.FIREFOX };
	private Test test;
	private AbstractPage cursor;
	private UserInteractionsTransition userInteractionsTransition;
	private final SynchronizedCommandStack syncCommandStack;
	private final AutoLayout autoLayout;
	private boolean enabled;
	private final Display display;
	
	public CubicRecorder(final Test test, SynchronizedCommandStack comandStack, AutoLayout autoLayout, Display display) {
		this.test = test;
		this.syncCommandStack = comandStack;
		this.autoLayout = autoLayout;
		this.display = display;
		//reuse empty start page if present:
		for(Transition t : test.getStartPoint().getOutTransitions()) {
			if(t.getEnd() instanceof Page) {
				setCursor((Page) t.getEnd());
			}
		}
		
		//if no empty start page, create new
		if(this.cursor == null && test.getStartPoint().getFirstNodeFromOutTransitions() == null) {
			syncCommandStack.execute(new Runnable() {
				public void run() {
					setCursor(createNewUserInteractionTransition(test.getStartPoint()));
				}
			});
		}
		//reset active transition to force creation of a new one for the user inputs
		this.userInteractionsTransition = null;
	}
	
	public CubicRecorder(Test test, Page cursor, SynchronizedCommandStack commandStack, AutoLayout autoLayout, Display display) {
		this.test = test;
		this.display = display;
		setCursor(cursor);
		this.syncCommandStack = commandStack;
		this.autoLayout = autoLayout;
	}
	
	
	/* (non-Javadoc)
	 * @see org.cubictest.recorder.IRecorder#setCursor(org.cubictest.model.AbstractPage)
	 */
	public void setCursor(AbstractPage page) {
		this.cursor = page;
		final AbstractPage cursor = this.cursor; 
		display.syncExec(new Runnable() {
			public void run() {
				autoLayout.setPageSelected(cursor);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.cubictest.recorder.IRecorder#addPageElement(org.cubictest.model.PageElement)
	 */
	public void addPageElement(PageElement element, PageElement parent) {
		if (!enabled) return;
		
		CreatePageElementCommand createElementCmd = new CreatePageElementCommand();
		if(parent != null && parent instanceof IContext){
			createElementCmd.setIndex(((IContext) parent).getRootElements().size());
			createElementCmd.setContext((IContext) parent);
		} else {
			createElementCmd.setIndex(cursor.getRootElements().size());
			createElementCmd.setContext(this.cursor);
		}
		createElementCmd.setPageElement(element);
		
		this.syncCommandStack.execute(createElementCmd);
		this.autoLayout.layout(cursor);
	}
	
	
	/* (non-Javadoc)
	 * @see org.cubictest.recorder.IRecorder#addUserInput(org.cubictest.model.UserInteraction)
	 */
	public void addUserInput(UserInteraction action, PageElement parent) {
		if (!enabled) return;
		
		if(this.userInteractionsTransition == null) {
			createNewUserInteractionTransition(this.cursor);
		}
		
		IContext ctx = (parent instanceof IContext) ? (IContext) parent : cursor;
		boolean unique = true;
		for (PageElement pe : ctx.getRootElements()) {
			pe.resetStatus();
			((PageElement) action.getElement()).resetStatus();
			if (pe.isEqualTo(action.getElement())) {
				unique = false;
				action.setElement(pe);
				break;
			}
		}
		if (unique) {
			this.addPageElement((PageElement) action.getElement(), parent);
		}

		this.userInteractionsTransition.addUserInteraction(action);
		this.autoLayout.layout(cursor);

		
		ActionType lastActionType = action.getActionType();
		if (lastActionType.equals(CLICK)) {
			IActionElement pe = action.getElement();
			if (pe instanceof Link || pe instanceof Button || pe instanceof Image) {
				advanceCursor();
			}
		}
	}

	/**
	 * Advance the cursor, creating a new user interaction transition if needed.
	 */
	private void advanceCursor() {
		if (this.userInteractionsTransition == null) {
			createNewUserInteractionTransition(this.cursor);
		}
		//advance the cursor:
		setCursor((AbstractPage) this.userInteractionsTransition.getEnd());
		this.userInteractionsTransition = null;
	}
	
	/**
	 * Create a new Page and a UserInteractionsTransition transition to it
	 */
	private AbstractPage createNewUserInteractionTransition(TransitionNode from) {
		Page page = new Page();
		if (from instanceof UrlStartPoint || from instanceof ExtensionStartPoint) {
			int num = from.getOutTransitions().size();
			page.setPosition(new Point(ITestEditor.INITIAL_PAGE_POS_X + (290 * num), ITestEditor.INITIAL_PAGE_POS_Y));
		}
				
		UserInteractionsTransition ua = new UserInteractionsTransition(from, page);
		
		/* Add Page */
		AddAbstractPageCommand addPageCmd = new AddAbstractPageCommand();
		addPageCmd.setPage(page);
		addPageCmd.setTest(test);
		syncCommandStack.execute(addPageCmd);

		/* Change Page Name */
		ChangeNameCommand changePageNameCmd = new ChangeNameCommand();
		changePageNameCmd.setNamePropertyObject(page);
		changePageNameCmd.setOldName("");
		if (from instanceof UrlStartPoint) {
			changePageNameCmd.setNewName("First Page");
		}
		else {
			changePageNameCmd.setNewName("next page");
		}
		syncCommandStack.execute(changePageNameCmd);
		
		/* Add Transition */
		CreateTransitionCommand createTransitionCmd = new CreateTransitionCommand();
		if (!(from instanceof ExtensionStartPoint)) {
			createTransitionCmd.setTransition(ua);
		}
		createTransitionCmd.setTest(test);
		createTransitionCmd.setSource(from);
		createTransitionCmd.setTarget(page);
		createTransitionCmd.setAutoCreateTargetPage(false);
		syncCommandStack.execute(createTransitionCmd);

		userInteractionsTransition = ua;
		return page;
	}

	public void setStateTitle(String title) {
		ChangeNameCommand changePageNameCmd = new ChangeNameCommand();
		if(userInteractionsTransition != null) {
			changePageNameCmd.setNamePropertyObject((AbstractPage) userInteractionsTransition.getEnd());
			changePageNameCmd.setOldName(userInteractionsTransition.getEnd().getName());
		} else {
			changePageNameCmd.setNamePropertyObject(cursor);
			changePageNameCmd.setOldName(cursor.getName());
		}
		changePageNameCmd.setNewName(title);	
		this.syncCommandStack.execute(changePageNameCmd);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void addToTest(final Transition transition, final AbstractPage endPage) {
		CreateTransitionCommand createTransitionCommand = new CreateTransitionCommand();
		createTransitionCommand.setTransition(transition);
		createTransitionCommand.setTest(test);
		this.syncCommandStack.execute(createTransitionCommand);

		AddAbstractPageCommand addAbstractPageCommand = new AddAbstractPageCommand();
		addAbstractPageCommand.setPage(endPage);
		addAbstractPageCommand.setTest(test);
		this.syncCommandStack.execute(addAbstractPageCommand);

		this.autoLayout.layout(transition.getStart());
	}
}
