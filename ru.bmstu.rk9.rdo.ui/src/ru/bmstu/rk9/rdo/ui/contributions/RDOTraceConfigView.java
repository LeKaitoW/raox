package ru.bmstu.rk9.rdo.ui.contributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import ru.bmstu.rk9.rdo.generator.RDONaming;
import ru.bmstu.rk9.rdo.lib.DecisionPointSearch.SerializationLevel;
import ru.bmstu.rk9.rdo.lib.TraceConfig;
import ru.bmstu.rk9.rdo.lib.TraceConfig.TraceNode;
import ru.bmstu.rk9.rdo.rdo.DecisionPoint;
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch;
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource;
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Pattern;
import ru.bmstu.rk9.rdo.rdo.RDOModel;
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

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                VIEW SETUP                                 /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

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

		traceTreeViewer.addCheckStateListener(
			new ICheckStateListener()
			{
				@Override
				public void checkStateChanged(CheckStateChangedEvent event)
				{
					boolean traceState = event.getChecked();
					TraceNode node = (TraceNode) event.getElement();

					node.setTraceState(traceState);
					traceTreeViewer.setSubtreeChecked(
						event.getElement(), traceState);
					node.traceVisibleChildren(traceState);
				}
			}
		);

		IPartService service =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService();

		service.addPartListener(new IPartListener2() {
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				String partId = partRef.getId();
				if (partId.substring(partId.lastIndexOf('.') + 1).equals("RDO")) {
					TraceNode modelNode = modelNodes.get(partRef);
					traceConfig.removeModel(modelNode);
					modelNodes.remove(partRef);
					List<TraceNode> modelsWithSameName = traceConfig
							.findModelsWithSameName(modelNode.getName());
					if (modelsWithSameName.size() == 1)
						modelsWithSameName.get(0).mustShowFullName(false);
					//TODO exception raised on refresh if there are no children
					//but without refresh tree is not cleared
					if (!traceTreeViewer.getControl().isDisposed()
							&& traceConfig.getRoot().hasChildren())
						traceTreeViewer.refresh();
				}
			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals("ru.bmstu.rk9.rdo.RDO")) {
					if (modelNodes.containsKey(partRef))
						return;

					IEditorPart editor = partRef.getPage().getActiveEditor();
					IXtextDocument document = ((XtextEditor) editor)
							.getDocument();

					EList<EObject> contents = document.readOnly(
							new IUnitOfWork<XtextResource, XtextResource>() {
								public XtextResource exec(XtextResource state) {
									return state;
								}
							}).getContents();

					if (contents.isEmpty())
						return;

					RDOModel model = (RDOModel) contents.get(0);
					TraceNode newModel = addModel(model.eResource());
					modelNodes.put(partRef, newModel);

					List<TraceNode> modelsWithSameName = traceConfig
							.findModelsWithSameName(newModel.getName());
					if (modelsWithSameName.size() > 1)
						for (TraceNode node : modelsWithSameName)
							node.mustShowFullName(true);

					if (traceTreeViewer.getInput() == null)
						traceTreeViewer.setInput(traceConfig);

					document.addModelListener(new IXtextModelListener() {
						@Override
						public void modelChanged(XtextResource resource) {
							updateInput(resource);
						}
					});
				}
			}

			private final Map<IWorkbenchPartReference, TraceNode> modelNodes =
					new HashMap<IWorkbenchPartReference, TraceNode>();
		});
	}

	public static TraceNode addModel(Resource model) {
		TraceNode modelNode = traceConfigurator
				.initModel(traceConfig.getRoot(), model);
		updateInput(model);
		return modelNode;
	}

	public static void updateInput(Resource model) {
		TraceNode modelNode = traceConfig.findModel(
				model.getURI().toPlatformString(false));
		traceConfigurator.fillCategories(model, modelNode);
		if (RDOTraceConfigView.traceTreeViewer == null)
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				RDOTraceConfigView.traceTreeViewer.refresh();
			}
		});
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

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                             HELPER CLASSES                                /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

class TraceConfigurator
{
	public enum TraceCategory
	{
		RESOURCES("Resources"),
		PATTERNS("Patterns"),
		DECISION_POINTS("Decision points"),
		RESULTS("Results");

		TraceCategory(String name)
		{
			this.name = name;
		}

		private final String name;

		public final String getName()
		{
			return name;
		}
	}

	public final void fillCategories(Resource model, TraceNode modelNode)
	{
		fillCategory(
			modelNode.getVisibleChildren().get(TraceCategory.RESOURCES.ordinal()),
			model,
			ResourceDeclaration.class
		);

		fillCategory(
			modelNode.getVisibleChildren().get(TraceCategory.PATTERNS.ordinal()),
			model,
			Pattern.class
		);

		fillCategory(
			modelNode.getVisibleChildren().get(TraceCategory.DECISION_POINTS.ordinal()),
			model,
			DecisionPoint.class
		);

		fillCategory(
			modelNode.getVisibleChildren().get(TraceCategory.RESULTS.ordinal()),
			model,
			ResultDeclaration.class
		);
	}

	private final <T extends EObject> void fillCategory(
		TraceNode category,
		Resource model,
		Class<T> categoryClass
	)
	{
		TreeIterator<EObject> allContents = model.getAllContents();
		category.hideChildren();
		final ArrayList<T> categoryList = new ArrayList<T>();
		Iterator<T> filter = Iterators.<T>filter(allContents, categoryClass);
		Iterable<T> iterable = IteratorExtensions.<T>toIterable(filter);
		Iterables.addAll(categoryList, iterable);

		for(T c : categoryList)
		{
			TraceNode child = category.addChild(RDONaming.getFullyQualifiedName(c));
			if(c instanceof Pattern)
			{
				for(EObject relRes : c.eContents())
				{
					if(relRes instanceof RuleRelevantResource ||
						relRes instanceof EventRelevantResource ||
						relRes instanceof OperationRelevantResource)
					child.addChild(
						child.getName() + "." + RDONaming.getNameGeneric(relRes));
				}
			}
			else if(c instanceof DecisionPointSearch)
			{
				for(SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getName() + "." + type.toString());
			}
		}
	}

	public final TraceNode initModel(TraceNode root, Resource model) {
		TraceNode modelNode = root.addChild(
				model.getURI().toPlatformString(false), false, true);
		for (TraceCategory category : TraceCategory.values())
			modelNode.addChild(category.getName());
		return modelNode;
	}
}

  /*――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――/
 /                                PROVIDERS                                  /
/――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――*/

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
		if(!traceConfig.getRoot().hasChildren())
			return null;
		return traceConfig.getRoot().getVisibleChildren().toArray();
	}

	public Object[] getChildren(Object parentElement)
	{
		TraceNode traceNode = (TraceNode) parentElement;
		if(!traceNode.hasChildren())
			return null;
		return traceNode.getVisibleChildren().toArray();
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
	public String getText(Object element) {
		TraceNode traceNode = (TraceNode) element;
		return traceNode.getRelativeName();
	}
}
