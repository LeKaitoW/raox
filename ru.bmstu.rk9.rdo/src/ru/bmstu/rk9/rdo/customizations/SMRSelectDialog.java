package ru.bmstu.rk9.rdo.customizations;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;


public class SMRSelectDialog
{
	private static class RunnableForDialog implements Runnable
	{
		private AtomicInteger result;
		private List<String> input;

		RunnableForDialog(AtomicInteger result, List<String> input)
		{
			this.result = result;
			this.input = input;
		}

		@Override
		public void run()
		{
			Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			ListDialog dialog = new ListDialog(activeShell)
			{
				@Override
				public void create()
				{
					setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
					super.create();
				}
			};

			dialog.setTitle("RDO Compiler");
			dialog.setMessage("Your project have multiple '$Simulation_run' definitions.\n"
					+ "Please specify the file with definition you want to use:");

			dialog.setContentProvider(new ArrayContentProvider());
			dialog.setLabelProvider(new ArrayLabelProvider());
			dialog.setHelpAvailable(false);

			dialog.setInput(input);
			dialog.setInitialSelections(new Object[]{input.get(0)});

			if (dialog.open() == Window.OK)
			{
				Object[] result = dialog.getResult();
				String rs = (String)(result[0]);
				System.out.println(rs);
				this.result.set(input.indexOf(rs));
			}
			else
				this.result.set(-1);
		}
	}

	public static int invoke(List<String> input)
	{
		AtomicInteger result = new AtomicInteger();

		PlatformUI.getWorkbench().getDisplay().syncExec(new RunnableForDialog(result, input));

		return result.get();
	}

	static class ArrayLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		@Override
		public String getText(Object element)
		{
			return (String)element;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			return (String)element;
		}

	}
}