package ru.bmstu.rk9.rdo.ui.contributions;

import java.util.ArrayList;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rdo.lib.Simulator;

public class RDOTraceView extends ViewPart
{
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOTraceView";

	static private TableViewer viewer;
	private static Display display;

	@Override
	public void createPartControl(Composite parent) {
		createViewer(parent);
	}

	private void createViewer(Composite parent)
	{
		display = getSite().getShell().getDisplay();
		viewer = new TableViewer(
			parent,
			SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL
		);

		FontRegistry fontRegistry =
			PlatformUI
			.getWorkbench()
			.getThemeManager()
			.getCurrentTheme()
			.getFontRegistry();

		viewer.setContentProvider(new RDOTraceViewContentProvider(viewer));
		viewer.setUseHashlookup(true);
		viewer.getTable().setFont(
			fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT));
	}

	public static void fireReady()
	{
		final ArrayList<String> traceList = Simulator.getTracer().getTraceList();
		display.asyncExec(
			new Runnable()
			{
				@Override
				public void run()
				{
					viewer.setInput(traceList);
					viewer.setItemCount(traceList.size());
				}
			}
		);
	}

	@Override
	public void setFocus() {}
}

class RDOTraceViewContentProvider implements ILazyContentProvider
{
	private TableViewer viewer;
	private ArrayList<String> traceList;

	public RDOTraceViewContentProvider(TableViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public void dispose() {}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		this.traceList = (ArrayList<String>) newInput;
	}

	@Override
	public void updateElement(int index)
	{
		viewer.replace(traceList.get(index), index);
	}
}


