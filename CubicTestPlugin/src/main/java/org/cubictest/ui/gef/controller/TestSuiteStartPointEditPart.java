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

import org.cubictest.model.ConnectionPoint;
import org.cubictest.model.TransitionNode;
import org.cubictest.ui.gef.view.AbstractTransitionNodeFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;


/**
 * Contoller for the <code>TestSuiteStartPoint</code> model.
 * 
 * @author Christian Schwarz
 */
public class TestSuiteStartPointEditPart extends AbstractStartPointEditPart {

	/**
	 * Constructor for <code>SubTestStartPoint</code>.
	 * @param point the model
	 */
	public TestSuiteStartPointEditPart(ConnectionPoint point) {
		super(point);
	}

	@Override
	protected String getName() {
		return "Test suite start point";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		
		AbstractTransitionNodeFigure startPointFigure = new AbstractTransitionNodeFigure();
		startPointFigure.setBackgroundColor(new Color(null, 238, 234, 222));
		startPointFigure.setLabelLength(200);
		Point p = ((TransitionNode)getModel()).getPosition();
		startPointFigure.setLocation(p);
		startPointFigure.setText(getName());
		startPointFigure.setToolTipText("The start point for the test suite.");
		return startPointFigure;
	}
	
}