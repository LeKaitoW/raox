package ru.bmstu.rk9.rdo.ui.contributions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import ru.bmstu.rk9.rdo.generator.RDONaming;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch.SerializationLevel;
import ru.bmstu.rk9.rdo.lib.ModelStructureHelper;
import ru.bmstu.rk9.rdo.lib.TraceConfig;
import ru.bmstu.rk9.rdo.lib.TraceConfig.TraceNode;
import ru.bmstu.rk9.rdo.rdo.DecisionPoint;
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource;
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Pattern;
import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration;
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration;
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource;

public class RDOTraceConfigView extends ViewPart
{
	public static final String ID = "ru.bmstu.rk9.rdo.ui.RDOTraceConfigView";

	private static CheckboxTreeViewer traceTreeViewer;

	private static TraceConfig traceConfig = new TraceConfig();
	private static TraceConfigurator traceConfigurator =
		new TraceConfigurator();

	@Override
	public void createPartControl(Composite parent)
	{
		traceTreeViewer = new CheckboxTreeViewer(parent);
		Tree traceTree = traceTreeViewer.getTree();
		traceTree.setLayoutData(new GridLayout());
		traceTree.setLinesVisible(true);

		traceTreeViewer.setContentProvider(
			new RDOTraceConfigContentProvider());
		traceTreeViewer.setLabelProvider(
			new RDOTraceConfigLabelProvider());
		traceTreeViewer.setCheckStateProvider(
			new RDOTraceConfigCheckStateProvider());

		traceConfigurator.initCategories(traceConfig.getRoot());
		traceTreeViewer.setInput(traceConfig);

		traceTreeViewer.addCheckStateListener(
			new ICheckStateListener()
			{
				@Override
				public void checkStateChanged(CheckStateChangedEvent event)
				{
					TraceNode node = (TraceNode) event.getElement();
					node.setTraceState(event.getChecked());
					if (event.getChecked())
					{
						traceTreeViewer.setSubtreeChecked(
							event.getElement(), true);
						node.traceVisibleChildren(true);
					}
				}
			}
		);
	}

	public static void updateInput(Resource model)
	{
		traceConfig.setModelName(RDONaming.getResourceName(model));
		traceConfigurator.fillCategories(model, traceConfig.getRoot());
		if (RDOTraceConfigView.traceTreeViewer == null)
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(
			new Runnable()
				{
					@Override
					public void run()
					{
						RDOTraceConfigView.traceTreeViewer.refresh();
					}
				}
			);
	}

	public static void onModelSave()
	{
		traceConfig.getRoot().removeHiddenChildren();
	}

	public static final void initNames()
	{
		traceConfig.initNames();
	}

	@Override
	public void setFocus()
	{}
}

class TraceConfigurator
{
	public enum TraceCategory
	{
		RESOURCES("Resources", ResourceDeclaration.class),
		PATTERNS("Patterns", Pattern.class),
		DECISION_POINTS("Decision points", DecisionPoint.class),
		RESULTS("Results", ResultDeclaration.class);

		TraceCategory(String name, Class<?> cateforyClass)
		{
			this.name = name;
			this.categoryClass = cateforyClass;
		}

		private final String name;

		public final String getName()
		{
			return name;
		}

		//TODO doesn't seem like a good way to work with Class type
		public final Class getCategoryClass()
		{
			return categoryClass;
		}

		private final Class<?> categoryClass;
	}

	public final void fillCategories(Resource model, TraceNode node)
	{
		//TODO see comment to category.getCategoryClass()
		for (TraceCategory category : TraceCategory.values())
			fillCategory(
				node.getChildren().get(category.ordinal()),
				model.getAllContents(),
				category.getCategoryClass()
			);
	}

	private final <T extends EObject> void fillCategory(
		TraceNode category,
		TreeIterator<EObject> allContents,
		Class<T> categoryClass
	)
	{
		category.hideChildren();
		final ArrayList<T> categoryList =
			new ArrayList<T>();
		Iterator<T> filter = Iterators.<T>filter(allContents, categoryClass);
		Iterable<T> iterable = IteratorExtensions.<T>toIterable(filter);
		Iterables.addAll(categoryList, iterable);

		//TODO why don't name include model name here already?
		//the way it is now it differs from the names we get from database
		for (T c : categoryList)
		{
			TraceNode child = category.addChild(RDONaming.getNameGeneric(c));
			if (categoryClass == Pattern.class)
			{
				for (EObject relRes : c.eContents())
				{
					if (relRes instanceof RuleRelevantResource ||
						relRes instanceof EventRelevantResource ||
						relRes instanceof OperationRelevantResource)
					child.addChild(
						child.getName() + "." + RDONaming.getNameGeneric(relRes));
				}
			}
			else if (categoryClass == DecisionPoint.class)
			{
				for (SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getName() + "." + type.toString());
			}
		}
	}

	public final void initCategories(TraceNode node)
	{
		for (TraceCategory category : TraceCategory.values())
			node.addChild(category.getName());
	}
}

class RDOTraceConfigCheckStateProvider implements ICheckStateProvider
{
	@Override
	public boolean isChecked(Object element)
	{
		TraceNode node = (TraceNode) element;
		return node.isTraced();
	}

	@Override
	public boolean isGrayed(Object element)
	{
		return false;
	}
}

class RDOTraceConfigContentProvider implements ITreeContentProvider
{
	public void dispose() {}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getElements(Object inputElement)
	{
		TraceConfig traceConfig = (TraceConfig) inputElement;
		if (!traceConfig.getRoot().hasChildren())
			return null;
		return traceConfig.getRoot().getChildren().toArray();
	}

	public Object[] getChildren(Object parentElement)
	{
		TraceNode traceNode = (TraceNode) parentElement;
		if (!traceNode.hasChildren())
			return null;
		return traceNode.getChildren().toArray();
	}

	public Object getParent(Object element)
	{
		TraceNode traceNode = (TraceNode) element;
		return traceNode.getParent();
	}

	public boolean hasChildren(Object element)
	{
		TraceNode traceNode = (TraceNode) element;
		return traceNode.hasChildren();
	}
}

class RDOTraceConfigLabelProvider implements ILabelProvider
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
		TraceNode traceNode = (TraceNode) element;
		return ModelStructureHelper.getRelativeName(traceNode.getName());
	}

}
