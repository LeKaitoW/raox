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

	@Override
	public void createPartControl(Composite parent)
	{
		createViewer(parent);
	}

	private final void createViewer(Composite parent)
	{
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

		viewer.setContentProvider(new RDOTraceViewContentProvider());
		viewer.setLabelProvider(new RDOTraceViewLabelProvider());
		viewer.setUseHashlookup(true);
		viewer.getTable().setFont(
			fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT));
	}

	public static RDOTraceUpdater updater = new RDOTraceUpdater();

	public static class RDOTraceUpdater implements Subscriber
	{
		@Override
		public void fireChange()
		{
			final ArrayList<Tracer.TraceOutput> traceList =
				Simulator.getTracer().getTraceList();
			if (RDOTraceView.viewer != null)
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

	@Override
	public void setFocus() {}
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
	private final EnumMap<Tracer.TraceType, TraceColor> colorByType =
		new EnumMap<Tracer.TraceType, TraceColor>(Tracer.TraceType.class);

	RDOTraceViewLabelProvider()
	{
		initializeColorMap();
	}

	private class TraceColor
	{
		private final Color foregroundColor;
		private final Color backgroundColor;

		public TraceColor(Color fg, Color bg)
		{
			foregroundColor = fg;
			backgroundColor = bg;
		}

		public final Color foregroundColor()
		{
			return foregroundColor;
		}

		public final Color backgroundColor()
		{
			return backgroundColor;
		}
	}

	private final void initializeColorMap()
	{
		Display display = PlatformUI.getWorkbench().getDisplay();
		colorByType.put(
			Tracer.TraceType.RESOURCE_CREATE,
			new TraceColor(
				new Color(display, 0x23, 0x74, 0x42),
				new Color(display, 0x96, 0xFF, 0x96)
			)
		);

		colorByType.put(
			Tracer.TraceType.RESOURCE_KEEP,
			new TraceColor(
				new Color(display, 0x00, 0x86, 0x00),
				new Color(display, 0xD0, 0xFF, 0xD0)
			)
		);

		colorByType.put(
			Tracer.TraceType.RESOURCE_ERASE,
			new TraceColor(
				new Color(display, 0x43, 0x5A, 0x43),
				new Color(display, 0xB4, 0xE0, 0xB4)
			)
		);

		colorByType.put(
			Tracer.TraceType.SYSTEM,
			new TraceColor(
				new Color(display, 0x8B, 0x00, 0x00),
				new Color(display, 0xFF, 0xC0, 0xCB)
			)
		);

		colorByType.put(
			Tracer.TraceType.OPERATION_BEGIN,
			new TraceColor(
				new Color(display, 0x34, 0x4B, 0xA2),
				new Color(display, 0xAA, 0xE3, 0xFB)
			)
		);

		colorByType.put(
			Tracer.TraceType.OPERATION_END,
			new TraceColor(
				new Color(display, 0x16, 0x02, 0x50),
				new Color(display, 0x81, 0xB0, 0xD5)
			)
		);

		colorByType.put(
			Tracer.TraceType.EVENT,
			new TraceColor(
				new Color(display, 0x4F, 0x29, 0x62),
				new Color(display, 0xD0, 0xD0, 0xFF)
			)
		);

		colorByType.put(
			Tracer.TraceType.RULE,
			new TraceColor(
				new Color(display, 0x17, 0x32, 0x47),
				new Color(display, 0xB6, 0xCB, 0xDB)
			)
		);

		colorByType.put(
			Tracer.TraceType.RESULT,
			new TraceColor(
				new Color(display, 0x00, 0x00, 0x00),
				new Color(display, 0xF1, 0xFB, 0xE2)
			)
		);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose()
	{
		for (final TraceColor color : colorByType.values())
		{
			color.foregroundColor().dispose();
			color.backgroundColor().dispose();
		}
	}

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
		Tracer.TraceType type = ((Tracer.TraceOutput) element).type();
		return colorByType.get(type).foregroundColor();
	}

	@Override
	public Color getBackground(Object element)
	{
		Tracer.TraceType type = ((Tracer.TraceOutput) element).type();
		return colorByType.get(type).backgroundColor();
	}
}
