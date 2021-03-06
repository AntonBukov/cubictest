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
package org.cubictest.ui.gef.command;

import org.cubictest.common.utils.ModelUtil;
import org.cubictest.model.CommonTransition;
import org.cubictest.model.Page;
import org.cubictest.model.Transition;
import org.cubictest.model.TransitionNode;
import org.eclipse.gef.commands.Command;


/**
 * A command that reconnect a <code>Transition</code>'s target to another <code>TransitonNode</code>.
 *
 * @author SK Skytteren 
 */
public class ReconnectTrasitionTargetCommand extends Command {

	private Transition transition;
	private TransitionNode newTarget;
	private TransitionNode oldTarget;
	private boolean isNoModelChanges;

	/**
	 * @param transition
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	/**
	 * @param newTarget
	 */
	public void setNewTarget(TransitionNode node) {
		this.newTarget = node;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		super.execute();
		if (isNoModelChanges)
			return;
		
		this.oldTarget = transition.getEnd();
		if (transition instanceof CommonTransition){
			((Page)oldTarget).removeCommonTransition((CommonTransition)transition);
			transition.setEnd(newTarget);
			((Page)newTarget).addCommonTransition((CommonTransition) transition);
		}else{
			oldTarget.setInTransition(null);
			transition.setEnd(newTarget);
			newTarget.setInTransition(transition);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		super.undo();
		if (isNoModelChanges)
			return;

		if (transition instanceof CommonTransition){
			((Page)newTarget).removeCommonTransition((CommonTransition)transition);
			transition.setEnd(oldTarget);
			((Page)oldTarget).addCommonTransition((CommonTransition) transition);
		}else{
			transition.setEnd(oldTarget);
			oldTarget.setInTransition(transition);
			newTarget.setInTransition(null);
		}
	}
	
	/*
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		int status = ModelUtil.isLegalTransition(transition.getStart(), newTarget, false, true);
		isNoModelChanges = (status == ModelUtil.TRANSITION_EDIT_NO_CHANGES);
		return (status == ModelUtil.TRANSITION_EDIT_NO_CHANGES || status == ModelUtil.TRANSITION_EDIT_VALID);
	}

}
