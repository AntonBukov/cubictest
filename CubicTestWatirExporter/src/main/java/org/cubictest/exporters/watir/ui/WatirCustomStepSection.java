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
package org.cubictest.exporters.watir.ui;

import org.cubictest.common.utils.ErrorHandler;
import org.cubictest.exporters.watir.WatirExporterPlugin;
import org.cubictest.model.customstep.data.CustomTestStepData;
import org.cubictest.model.customstep.data.CustomTestStepDataEvent;
import org.cubictest.model.customstep.data.ICustomTestStepDataListener;
import org.cubictest.ui.customstep.section.CustomStepSection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

public class WatirCustomStepSection extends CustomStepSection implements ICustomTestStepDataListener{

	private IProject project;
	private CustomTestStepData data;
	private Link newRbFileLink;
	private Text classText;
	private Button browserRbFileButton;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(ColorConstants.white);
		
		FormLayout formLayout = new FormLayout();
		composite.setLayout(formLayout);
		
		newRbFileLink = new Link(composite, SWT.PUSH);
		newRbFileLink.setText("<A>CubicTest Watir extension*: </A>");
		newRbFileLink.setBackground(ColorConstants.white);
		newRbFileLink.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if(data.getPath() != null && data.getPath().length() > 0){
					
					IPath path = Path.fromPortableString(data.getPath());
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					if(file.exists()){
						IWorkbenchPage page = WatirExporterPlugin.getDefault().getWorkbench().
							getActiveWorkbenchWindow().getActivePage();
						try {
							IDE.openEditor(page, file, true);
							return;
						} catch (PartInitException ex) {
							ErrorHandler.logAndRethrow(ex);
						}
					}
				} else {
					// create watir file dialog
					BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
					IWorkbench workbench = WatirExporterPlugin.getDefault().getWorkbench();
					wizard.init(workbench, new StructuredSelection(project));
					WizardDialog dialog = new WizardDialog(new Shell(), wizard);
					if(dialog.open() == WizardDialog.OK){
						//TODO: Implement...
					}
				}
					
			}
		});
		
		FormData layoutData = new FormData();
		layoutData.left = new FormAttachment(0,0);
		layoutData.width = STANDARD_LABEL_WIDTH;
		newRbFileLink.setLayoutData(layoutData);
		
		classText = new Text(composite,SWT.BORDER);
		classText.setBackground(ColorConstants.white);
		classText.setText(data.getDisplayText());
		
		layoutData = new FormData();
		layoutData.left = new FormAttachment(newRbFileLink);
		layoutData.width = STANDARD_LABEL_WIDTH * 2;
		classText.setLayoutData(layoutData);
		
		browserRbFileButton = new Button(composite, SWT.PUSH);
		browserRbFileButton.setText("Browse...");
		browserRbFileButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				//TODO: implement something smart for browsing after a watir custom test step file..
			}
		});
		layoutData = new FormData();
		layoutData.left = new FormAttachment(classText,5);
		browserRbFileButton.setLayoutData(layoutData);
	}

	@Override
	public String getDataKey(){
		return "org.cubictest.watirexporter";
	}

	@Override
	public void setData(CustomTestStepData data) {
		this.data = data;
		data.addChangeListener(this);
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	public void handleEvent(CustomTestStepDataEvent event) {
		classText.setText(data.getDisplayText());
	}

}
