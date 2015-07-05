package ru.bmstu.rk9.rao.ui.contributions;

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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
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

import ru.bmstu.rk9.rao.rao.DecisionPointSearch;
import ru.bmstu.rk9.rao.rao.DecisionPointSome;
import ru.bmstu.rk9.rao.rao.Event;
import ru.bmstu.rk9.rao.rao.Pattern;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.rao.ResourceCreateStatement;
import ru.bmstu.rk9.rao.rao.Result;
import ru.bmstu.rk9.rao.generator.RaoNaming;
import ru.bmstu.rk9.rao.lib.Database.SerializationCategory;
import ru.bmstu.rk9.rao.lib.DecisionPointSearch.SerializationLevel;
import ru.bmstu.rk9.rao.lib.SerializationConfig;
import ru.bmstu.rk9.rao.lib.SerializationConfig.SerializationNode;

public class SerializationConfigView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.SerializationConfigView";

	private static CheckboxTreeViewer serializationTreeViewer;

	private static SerializationConfig serializationConfig = new SerializationConfig();
	private static SerializationConfigurator serializationConfigurator = new SerializationConfigurator();

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------- VIEW SETUP ----------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	@Override
	public void createPartControl(Composite parent) {
		serializationTreeViewer = new CheckboxTreeViewer(parent);
		Tree serializationTree = serializationTreeViewer.getTree();
		serializationTree.setLayoutData(new GridLayout());
		serializationTree.setLinesVisible(true);

		serializationConfig.clear();

		serializationTreeViewer
				.setContentProvider(new RaoSerializationConfigContentProvider());
		serializationTreeViewer
				.setLabelProvider(new RaoSerializationConfigLabelProvider());
		serializationTreeViewer
				.setCheckStateProvider(new RaoSerializationConfigCheckStateProvider());

		serializationTreeViewer
				.addCheckStateListener(new ICheckStateListener() {
					@Override
					public void checkStateChanged(CheckStateChangedEvent event) {
						boolean serializationState = event.getChecked();
						SerializationNode node = (SerializationNode) event
								.getElement();

						node.setSerializationState(serializationState);
						serializationTreeViewer.setSubtreeChecked(
								event.getElement(), serializationState);
						node.setSerializeVisibleChildren(serializationState);
					}
				});

		serializationTreeViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						Object item = serializationTreeViewer.getTree()
								.getSelection()[0].getData();
						if (item == null)
							return;

						if (serializationTreeViewer.getExpandedState(item))
							serializationTreeViewer.collapseToLevel(item, 1);
						else
							serializationTreeViewer.expandToLevel(item, 1);
					}
				});

		IPartService service = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPartService();

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
				if (partId.substring(partId.lastIndexOf('.') + 1).equals("Rao")) {
					SerializationNode modelNode = modelNodes.get(partRef);
					serializationConfig.removeModel(modelNode);
					modelNodes.remove(partRef);
					List<SerializationNode> modelsWithSameName = serializationConfig
							.findModelsWithSameName(modelNode.getFullName());
					if (modelsWithSameName.size() == 1)
						modelsWithSameName.get(0).mustShowFullName(false);
					if (!serializationTreeViewer.getControl().isDisposed())
						serializationTreeViewer.refresh();
				}
			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals("ru.bmstu.rk9.rao.Rao")) {
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

					RaoModel model = (RaoModel) contents.get(0);
					SerializationNode newModel = addModel(model.eResource());
					modelNodes.put(partRef, newModel);

					List<SerializationNode> modelsWithSameName = serializationConfig
							.findModelsWithSameName(newModel.getFullName());
					if (modelsWithSameName.size() > 1)
						for (SerializationNode node : modelsWithSameName)
							node.mustShowFullName(true);

					if (serializationTreeViewer.getInput() == null)
						serializationTreeViewer.setInput(serializationConfig);

					document.addModelListener(new IXtextModelListener() {
						@Override
						public void modelChanged(XtextResource resource) {
							updateInput(resource);
						}
					});
				}
			}

			private final Map<IWorkbenchPartReference, SerializationNode> modelNodes = new HashMap<IWorkbenchPartReference, SerializationNode>();
		});

		setEnabled(true);
	}

	public static void setEnabled(boolean state) {
		if (!readyForInput())
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				serializationTreeViewer.getTree().setEnabled(state);
			}
		});
	}

	public static SerializationNode addModel(Resource model) {
		SerializationNode modelNode = serializationConfigurator.initModel(
				serializationConfig.getRoot(), model);
		updateInput(model);
		return modelNode;
	}

	public static void updateInput(Resource model) {
		if (!readyForInput())
			return;
		SerializationNode modelNode = serializationConfig.findModel(model
				.getURI().toPlatformString(false));
		serializationConfigurator.fillCategories(model, modelNode);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				SerializationConfigView.serializationTreeViewer.refresh();
			}
		});
	}

	public static void onModelSave() {
		serializationConfig.getRoot().removeHiddenChildren();
	}

	public static final void initNames() {
		serializationConfig.initNames();
	}

	public static final SerializationConfig getConfig() {
		return serializationConfig;
	}

	@Override
	public void setFocus() {
	}

	public final static boolean readyForInput() {
		return serializationTreeViewer != null
				&& !serializationTreeViewer.getTree().isDisposed()
				&& serializationTreeViewer.getContentProvider() != null
				&& serializationTreeViewer.getLabelProvider() != null;
	}
}

// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
// ---------------------------- HELPER CLASSES ----------------------------- //
// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

class SerializationConfigurator {
	public final void fillCategories(Resource model, SerializationNode modelNode) {
		for (SerializationNode category : modelNode.getVisibleChildren())
			category.hideChildren();

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.RESOURCES.ordinal()), model,
				ResourceCreateStatement.class);

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.PATTERNS.ordinal()), model,
				Pattern.class);

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.EVENTS.ordinal()), model,
				Event.class);

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.DECISION_POINTS.ordinal()),
				model, DecisionPointSome.class);

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.RESULTS.ordinal()), model,
				Result.class);

		fillCategory(
				modelNode.getVisibleChildren().get(
						SerializationCategory.SEARCH.ordinal()), model,
				DecisionPointSearch.class);
	}

	private final <T extends EObject> void fillCategory(
			SerializationNode category, Resource model, Class<T> categoryClass) {
		final List<T> categoryItems = filterAllContents(model.getAllContents(),
				categoryClass);

		final Map<String, Integer> instanceCountOfResourceType = new HashMap<String, Integer>();

		for (T categoryItem : categoryItems) {
			String name = RaoNaming.getFullyQualifiedName(categoryItem);

			if (categoryItem instanceof ResourceCreateStatement) {
				if (!(categoryItem.eContainer() instanceof RaoModel))
					continue;
				if (((ResourceCreateStatement) categoryItem).getName() == null) {
					final String typeName = ((ResourceCreateStatement) categoryItem)
							.getType().getName();
					int count = 0;

					if (instanceCountOfResourceType.containsKey(typeName)) {
						count = instanceCountOfResourceType.get(typeName) + 1;
					}
					instanceCountOfResourceType.put(typeName, count);

					name = name.substring(0, name.lastIndexOf('.') + 1)
							+ typeName + "[" + count + "]";
				}
			}

			SerializationNode child = category.addChild(name);

			if (categoryItem instanceof DecisionPointSearch) {
				for (SerializationLevel type : SerializationLevel.values())
					child.addChild(child.getFullName() + "." + type.toString());
			}
			if (categoryItem instanceof Pattern
					|| categoryItem instanceof Event) {
				child.addChild(child.getFullName() + ".createdResources");
			}
		}
	}

	private final <T extends EObject> List<T> filterAllContents(
			TreeIterator<EObject> allContents, Class<T> categoryClass) {
		final ArrayList<T> categoryList = new ArrayList<T>();
		Iterator<T> filter = Iterators.<T> filter(allContents, categoryClass);
		Iterable<T> iterable = IteratorExtensions.<T> toIterable(filter);
		Iterables.addAll(categoryList, iterable);
		return categoryList;
	}

	public final SerializationNode initModel(SerializationNode root,
			Resource model) {
		SerializationNode modelNode = root.addChild(model.getURI()
				.toPlatformString(false), false, true);
		for (SerializationCategory category : SerializationCategory.values())
			modelNode.addChild(modelNode.getName() + "." + category.getName());
		return modelNode;
	}
}

// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
// ------------------------------- PROVIDERS ------------------------------- //
// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

class RaoSerializationConfigCheckStateProvider implements ICheckStateProvider {
	@Override
	public boolean isChecked(Object element) {
		SerializationNode node = (SerializationNode) element;
		return node.isSerialized();
	}

	@Override
	public boolean isGrayed(Object element) {
		return false;
	}
}

class RaoSerializationConfigContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		SerializationConfig serializationConfig = (SerializationConfig) inputElement;
		if (!serializationConfig.getRoot().hasChildren())
			return new Object[] {};
		return serializationConfig.getRoot().getVisibleChildren().toArray();
	}

	public Object[] getChildren(Object parentElement) {
		SerializationNode serializationNode = (SerializationNode) parentElement;
		if (!serializationNode.hasChildren())
			return new Object[] {};
		return serializationNode.getVisibleChildren().toArray();
	}

	public Object getParent(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.getParent();
	}

	public boolean hasChildren(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.hasChildren();
	}
}

class RaoSerializationConfigLabelProvider implements ILabelProvider {
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.getName();
	}
}
