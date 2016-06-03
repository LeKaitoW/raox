package ru.bmstu.rk9.rao.ui.player.gui;

/******************************************************************************
 * Copyright (c) 1998, 2004 Jackwind Li Guojie
 * All right reserved. 
 * 
 * Created on Feb 8, 2004 3:43:08 PM by JACK
 * $Id$
 * 
 * visit: http://www.asprise.com/swt
 *****************************************************************************/
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DropDownAndSimple {
	Display display = new Display();
	Shell shell = new Shell(display);
	Combo comboDropDown = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);

	public DropDownAndSimple() {
		init();

		RowLayout rowLayout = new RowLayout();
		rowLayout.spacing = 15;
		rowLayout.marginWidth = 15;
		rowLayout.marginHeight = 15;

		shell.setLayout(rowLayout);

		comboDropDown.add("sec ");
		comboDropDown.add("min ");
		comboDropDown.add("hours ");
		comboDropDown.add("days ");
		comboDropDown.add("mounths ");
		comboDropDown.add("years ");

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		display.dispose();
	}

	public void getSelected() {

		System.out.println("Index " + comboDropDown.getSelectionIndex());
		System.out.println("" + comboDropDown.getSelection());

	}

	private void init() {

	}

	public static void main(String[] args) {
		System.out.println("");

		DropDownAndSimple simple = new DropDownAndSimple();
		simple.getSelected();
	}
}
