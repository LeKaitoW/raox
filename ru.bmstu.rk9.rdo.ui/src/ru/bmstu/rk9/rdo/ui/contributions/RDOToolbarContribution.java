package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.swt.SWT;

import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.widgets.Scale;

public class RDOToolbarContribution extends WorkbenchWindowControlContribution
{
	private Scale speed; 

	@Override
	protected Control createControl(Composite parent)
	{
	    Composite container = new Composite(parent, SWT.NONE);

	    GridLayout gl = new GridLayout(2, false);
	    gl.marginTop = 0;
	    gl.marginHeight = 0;
	    gl.marginWidth = 0;
	    container.setLayout(gl);

	    GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
	    gd.widthHint = 100;

	    speed = new Scale(container, SWT.HORIZONTAL);
	    speed.setMinimum(0);
	    speed.setMaximum(100);
	    speed.setIncrement(5);
	    speed.setPageIncrement(10);
	    speed.setLayoutData(gd);
	    speed.setSelection(100);
	    speed.setEnabled(false);

		return container;
	}
}
