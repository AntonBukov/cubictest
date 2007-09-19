/*
 * Created on 17.may.2006
 * 
 * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
 * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
 *
 */
package org.cubictest.ui.sections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cubictest.model.CustomTestStepHolder;
import org.cubictest.model.customstep.CustomTestStep;
import org.cubictest.model.customstep.CustomTestStepParameter;
import org.cubictest.ui.gef.editors.GraphicalTestEditor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class CustomTestStepInputSection extends AbstractPropertySection {	
	private Composite composite;
	private List<CustomTestStepParameterComposite> composites = new ArrayList<CustomTestStepParameterComposite>();
	private CustomTestStepHolder customTestStepHolder;
	private Composite parent;

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof EditPart);
		this.customTestStepHolder = (CustomTestStepHolder) ((EditPart) input).getModel();
		
		updateInput(part);
		
	}


	private void updateInput(IWorkbenchPart part) {
		Iterator<CustomTestStepParameterComposite> idComIterator = composites.iterator();
		CustomTestStep customTestStep = customTestStepHolder.getCustomTestStep();
		List<CustomTestStepParameterComposite> newIdComs = new ArrayList<CustomTestStepParameterComposite>();
		for(Object obj : customTestStep.getParameters().toArray()){
			CustomTestStepParameterComposite parameterComposite;
			if (idComIterator.hasNext())
				parameterComposite = idComIterator.next();
			else{
				parameterComposite = new CustomTestStepParameterComposite(
					composite, getWidgetFactory(),	75);
				newIdComs.add(parameterComposite);
			}
			if(part instanceof GraphicalTestEditor){
				parameterComposite.setPart((GraphicalTestEditor) part);
			}
			parameterComposite.setValue(customTestStepHolder.getValue((CustomTestStepParameter) obj));
			parameterComposite.setVisible(true);
		}
		while(idComIterator.hasNext()){
			CustomTestStepParameterComposite parameterComposite = idComIterator.next();
			parameterComposite.setVisible(false);
		}
		composites.addAll(newIdComs);
		refresh();
	}


	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		this.parent = parent;
		if(composite == null)
			composite = getWidgetFactory().createFlatFormComposite(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		composite.setLayout(layout);
			
		Button refreshButton = getWidgetFactory().createButton(composite, "Refresh", SWT.PUSH);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.END;
		refreshButton.setLayoutData(data);
		refreshButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent event) {
				super.widgetDefaultSelected(event);
				customTestStepHolder.reloadCustomTestStep();
				updateInput(getPart());
			}
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		
		parent.pack(true);
		parent.redraw();
		parent.update();
		
	}
	
	@Override
	public void aboutToBeHidden() {
		super.aboutToBeHidden();
		for (CustomTestStepParameterComposite composite : composites) {
			composite.removeListeners();
		}
	}
}