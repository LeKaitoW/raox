package ru.bmstu.rk9.rdo.ui.contributions;

import java.util.ArrayList;
import java.util.EnumMap;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Subscriber;
import ru.bmstu.rk9.rdo.lib.Tracer;

public class RDOTraceView extends ViewPart
{
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOTraceView";

	static TableViewer viewer;

	public static RDOTraceUpdater updater;

	public static final EnumMap<Tracer.TraceType, Color> colorByType =
		new EnumMap<Tracer.TraceType, Color>(Tracer.TraceType.class);

	@Override
	public void createPartControl(Composite parent)
	{
		createViewer(parent);
	}

	private final void initializeColorMap()
	{
		//TODO choose proper colors
		//customs colors need to be created and disposed of
		colorByType.put(
			Tracer.TraceType.RESOURCE_CREATE,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_CYAN)
		);

		colorByType.put(
			Tracer.TraceType.RESOURCE_KEEP,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GREEN)
		);

		colorByType.put(
			Tracer.TraceType.RESOURCE_ERASE,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN)
		);

		colorByType.put(
			Tracer.TraceType.SYSTEM,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED)
		);

		colorByType.put(
			Tracer.TraceType.OPERATION_BEGIN,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE)
		);

		colorByType.put(
			Tracer.TraceType.OPERATION_END,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE)
		);

		colorByType.put(
			Tracer.TraceType.EVENT,
			PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_YELLOW)
		);

		colorByType.put(
			Tracer.TraceType.RULE,
			Display.getCurrent().getSystemColor(SWT.COLOR_GRAY)
		);

		colorByType.put(
			Tracer.TraceType.RESULT,
			Display.getCurrent().getSystemColor(SWT.COLOR_WHITE)
		);
	}

	private final void createViewer(Composite parent)
	{
		initializeColorMap();
		viewer = new TableViewer(
			parent,
			SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL
		);
		updater = new RDOTraceUpdater();

		FontRegistry fontRegistry =
			PlatformUI
			.getWorkbench()
			.getThemeManager()
			.getCurrentTheme()
			.getFontRegistry();

		viewer.setContentProvider(new RDOTraceViewContentProvider());
		viewer.setLabelProvider(new RDOTraceViewLabelProvider());
		viewer.setUseHashlookup(true);
		viewer.getTable().setFont(
			fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT));
	}

	@Override
	public void setFocus() {}

	public class RDOTraceUpdater implements Subscriber
	{
		@Override
		public void fireChange()
		{
			final ArrayList<Tracer.TraceOutput> traceList =
				Simulator.getTracer().getTraceList();
			PlatformUI.getWorkbench().getDisplay().asyncExec(
				new Runnable()
				{
					@Override
					public void run()
					{
						RDOTraceView.viewer.setInput(traceList);
						RDOTraceView.viewer.setItemCount(traceList.size());
					}
				}
			);
		}
	}
}

class RDOTraceViewContentProvider implements ILazyContentProvider
{
	private ArrayList<String> traceList;

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
		RDOTraceView.viewer.replace(traceList.get(index), index);
	}
}

//TODO maybe ITableLableProvider is more suitable
// but since there is only one column, currently there is no difference
class RDOTraceViewLabelProvider implements ILabelProvider, IColorProvider
{
	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		return ((Tracer.TraceOutput) element).content();
	}

	@Override
	public Color getForeground(Object element)
	{
		return null;
	}

	@Override
	public Color getBackground(Object element)
	{
		Tracer.TraceType type = ((Tracer.TraceOutput) element).type();
		return RDOTraceView.colorByType.get(type);
	}
}
