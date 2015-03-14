package ru.bmstu.rk9.rdo.ui.contributions;

import org.eclipse.swt.SWT;

import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.widgets.Label;

public class RDOToolbarSeparator extends WorkbenchWindowControlContribution {
	private Label separator;

	@Override
	protected Control createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayout gl = new GridLayout(1, false);
		gl.marginTop = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		container.setLayout(gl);

		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		gd.widthHint = 10;

		separator = new Label(container, SWT.SEPARATOR);
		separator.setLayoutData(gd);

		return container;
	}
}
