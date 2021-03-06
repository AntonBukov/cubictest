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
package org.cubictest.ui.gef.controller;

import java.util.List;

import org.cubictest.common.utils.ViewUtil;
import org.cubictest.model.PageElement;
import org.cubictest.model.context.AbstractContext;
import org.cubictest.model.context.Frame;
import org.cubictest.model.context.IContext;
import org.cubictest.ui.gef.directEdit.CubicTestDirectEditManager;
import org.cubictest.ui.gef.directEdit.CubicTestEditorLocator;
import org.cubictest.ui.gef.policies.ContextContainerEditPolicy;
import org.cubictest.ui.gef.policies.ContextLayoutEditPolicy;
import org.cubictest.ui.gef.policies.PageElementComponentEditPolicy;
import org.cubictest.ui.gef.policies.PageElementDirectEditPolicy;
import org.cubictest.ui.gef.view.CubicTestGroupFigure;
import org.cubictest.ui.gef.view.CubicTestImageRegistry;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Image;


/**
 * Edit part for AbstractContext model objects.
 * 
 * @author skyt
 * @author chr_schwarz
 */
public class ContextEditPart extends PageElementEditPart {

	public ContextEditPart(AbstractContext context) {
		setModel(context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		ContextLayoutEditPolicy layoutPolicy = new ContextLayoutEditPolicy((IContext)getModel());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, layoutPolicy);
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ContextContainerEditPolicy(layoutPolicy));
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new PageElementComponentEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new PageElementDirectEditPolicy());
	}
	
	@Override
	public AbstractContext getModel() {
		return (AbstractContext) super.getModel();
	}
	
	@Override
	public List<?> getModelChildren() {
		return ((AbstractContext)getModel()).getRootElements();
	}

	@Override
	protected CubicTestGroupFigure createFigure() {
		CubicTestGroupFigure figure = 
			new CubicTestGroupFigure(getModel().getText(), false);
		figure.setBackgroundColor(ColorConstants.listBackground);
		figure.getHeader().setIcon(getImage(getModel().isNot()));
		if(getModel() instanceof Frame){
			figure.setTooltipText("Check (i)frame present: $labelText"
					+ "\nFrames are used for identyfying a part of the page or a single page element.");
		}else{
			figure.setTooltipText("Check context present: $labelText"
				+ "\nContexts are used for identyfying a part of the page or a single page element.");
		}
		return figure;
	}
	@Override
	public void startDirectEdit(){
		if (manager == null)
			manager = new CubicTestDirectEditManager(this,
					TextCellEditor.class,
					new CubicTestEditorLocator(
							((CubicTestGroupFigure)getFigure()).getHeader()),
					((PageElement)getModel()).getText());
		manager.setText(((PageElement)getModel()).getText());
		manager.show();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#setSelected(int)
	 */
	@Override
	public void setSelected(int value) {
		super.setSelected(value);
		CubicTestGroupFigure figure = (CubicTestGroupFigure) getFigure();
		if (value != EditPart.SELECTED_NONE)
			figure.setSelected(true);
		else
			figure.setSelected(false);
		figure.repaint();
		CommandStack stack = getViewer().getEditDomain().getCommandStack();
		if (manager == null && ViewUtil.pageElementHasJustBeenCreated(stack, getModel()))
			startDirectEdit();
	}

	@Override
	protected Image getImage(boolean isNot) {
		if(getModel() instanceof Frame)
			return CubicTestImageRegistry.get(CubicTestImageRegistry.FRAME_IMAGE, isNot);
		return CubicTestImageRegistry.get(CubicTestImageRegistry.CONTEXT_IMAGE, isNot);
	}
}
