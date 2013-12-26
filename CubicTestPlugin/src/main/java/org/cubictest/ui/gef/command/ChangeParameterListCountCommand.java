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

import org.cubictest.model.Test;
import org.cubictest.model.parameterization.ParameterList;
import org.eclipse.gef.commands.Command;


public class ChangeParameterListCountCommand extends Command {

	private ParameterList list;
	private int count;
	private int oldCount;
	private Test test;

	public void setNewCount(int index) {
		this.count = index;
	}

	public void setParameterList(ParameterList list) {
		this.list = list;
	}
	
	public void setTest(Test test){
		this.test = test;
	}
	
	@Override
	public void execute() {
		oldCount = list.getParameterCount();
		list.setParameterCount(count);
	}
	
	@Override
	public void undo() {
		list.setParameterCount(oldCount);
	}
}
