package ru.bmstu.rk9.rao.ui.serialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import com.google.inject.Inject;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.ui.build.ModelBuilder;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfig.SerializationNode;

public class SerializationConfigView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.SerializationConfigView";

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------------- API ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	/**
	 * Set all elements of tree to checked state.
	 *
	 * @return true on success, false if failed to check all elements (tree is
	 *         not yet initialized)
	 */
	public final boolean checkAll() {
		return setStateForAll(true);
	}

	/**
	 * Set all elements of tree to unchecked state.
	 *
	 * @return true on success, false if failed to uncheck all elements (tree is
	 *         not yet initialized)
	 */
	public final boolean uncheckAll() {
		return setStateForAll(false);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------------- API HELPERS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final boolean setStateForAll(boolean state) {
		if (!readyForInput())
			return false;

		SerializationConfig serializationConfig = (SerializationConfig) serializationTreeViewer
				.getInput();
		if (serializationConfig == null)
			return false;

		SerializationNode root = serializationConfig.getRoot();

		for (SerializationNode node : root.getVisibleChildren()) {
			node.setSerializationState(state);
			serializationTreeViewer.setSubtreeChecked(node, state);
			node.setSerializeVisibleChildren(state);
		}

		serializationTreeViewer.refresh();
		return true;
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ------------------------------ VIEW SETUP --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private static CheckboxTreeViewer serializationTreeViewer;

	private static SerializationConfig serializationConfig = new SerializationConfig();
	private static SerializationConfigurator serializationConfigurator = new SerializationConfigurator();

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
						serializationTreeViewer.setSubtreeChecked(node,
								serializationState);
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

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.addPartListener(partListener);

		initializeSubscribers();
		setEnabled(true);
	}

	@Override
	public void dispose() {
		deinitializeSubscribers();
		super.dispose();
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

	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------- TREE UPDATES ---------------------------- //
	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	@Inject
	IResourceSetProvider resourceSetProvider;

	private final IPartListener2 partListener = new IPartListener2() {
		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part == null)
				return;
			if (!(part instanceof IEditorPart))
				return;

			IEditorPart editor = (IEditorPart) part;
			registerModel(editor);
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
			IWorkbenchPart part = partRef.getPart(false);
			if (part == null)
				return;
			if (!(part instanceof IEditorPart))
				return;

			IEditorPart editor = (IEditorPart) part;
			unregisterModel(editor);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
		}
	};

	private final Map<IResource, SerializationNode> modelNodes = new HashMap<>();
	private final Map<IProject, Set<IEditorPart>> projectReferences = new HashMap<>();

	private final void registerModel(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input == null)
			return;

		IResource resource = ResourceUtil.getFile(editor.getEditorInput());
		IProject project = resource.getProject();

		if (!projectReferences.containsKey(project))
			projectReferences.put(project, new HashSet<IEditorPart>());

		final List<IResource> projectFiles = ModelBuilder
				.getAllRaoFilesInProject(project);

		for (IResource raoFile : projectFiles) {
			URI uri = ModelBuilder.getURI(raoFile);
			if (modelNodes.containsKey(raoFile))
				continue;

			final ResourceSet resourceSet = resourceSetProvider.get(project);
			Resource loadedResource = resourceSet.getResource(uri, true);
			EList<EObject> contents = loadedResource.getContents();

			if (contents.isEmpty())
				continue;

			RaoModel model = (RaoModel) contents.get(0);
			SerializationNode newModel = addModel(model.eResource());
			modelNodes.put(raoFile, newModel);

			List<SerializationNode> modelsWithSameName = serializationConfig
					.findModelsWithSameName(newModel.getFullName());
			if (modelsWithSameName.size() > 1)
				for (SerializationNode node : modelsWithSameName)
					node.mustShowFullName(true);

			if (serializationTreeViewer.getInput() == null)
				serializationTreeViewer.setInput(serializationConfig);
		}

		Set<IEditorPart> projectSites = projectReferences.get(project);
		if (projectSites.contains(editor))
			return;

		projectSites.add(editor);

		if (!(editor instanceof XtextEditor))
			return;

		((XtextEditor) editor).getDocument().addModelListener(
				new IXtextModelListener() {
					@Override
					public void modelChanged(XtextResource resource) {
						updateInput(resource);
					}
				});

		return;
	}

	private final IProject removeEditorReference(IEditorPart site) {
		Iterator<Entry<IProject, Set<IEditorPart>>> iterator = projectReferences
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<IProject, Set<IEditorPart>> entry = iterator.next();
			Set<IEditorPart> projectSites = entry.getValue();
			if (!projectSites.contains(site))
				continue;

			projectSites.remove(site);
			if (projectSites.isEmpty()) {
				IProject project = entry.getKey();
				iterator.remove();
				return project;
			}
		}
		return null;
	}

	private final void unregisterModel(IEditorPart editor) {
		IProject unregisteredProject = removeEditorReference(editor);
		if (unregisteredProject == null)
			return;

		final List<IResource> projectFiles = ModelBuilder
				.getAllRaoFilesInProject(unregisteredProject);

		for (IResource raoFile : projectFiles) {
			if (!modelNodes.containsKey(raoFile))
				continue;

			SerializationNode modelNode = modelNodes.get(raoFile);
			serializationConfig.removeModel(modelNode);
			modelNodes.remove(raoFile);

			List<SerializationNode> modelsWithSameName = serializationConfig
					.findModelsWithSameName(modelNode.getFullName());
			if (modelsWithSameName.size() == 1)
				modelsWithSameName.get(0).mustShowFullName(false);
		}

		if (!serializationTreeViewer.getControl().isDisposed())
			serializationTreeViewer.refresh();
	}

	private static SerializationNode addModel(Resource model) {
		SerializationNode modelNode = serializationConfigurator.initModel(
				serializationConfig.getRoot(), model);
		updateInput(model);
		return modelNode;
	}

	private static void updateInput(Resource model) {
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
		serializationConfig.saveTreeAsNamesList();
	}

	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// --------------------------- SUBSCRIPTIONS ---------------------------- //
	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final void initializeSubscribers() {
		subscriberRegistrationManager.initialize(Arrays.asList(
				new SimulatorSubscriberInfo(enableSubscriber,
						ExecutionState.EXECUTION_COMPLETED),
				new SimulatorSubscriberInfo(disableSubscriber,
						ExecutionState.EXECUTION_STARTED)));
	}

	private final void deinitializeSubscribers() {
		subscriberRegistrationManager.deinitialize();
	}

	private final static SimulatorSubscriberManager subscriberRegistrationManager = new SimulatorSubscriberManager();

	private final static Subscriber enableSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			setEnabled(true);
		}
	};

	private final static Subscriber disableSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			setEnabled(false);
		}
	};

	private static void setEnabled(boolean state) {
		if (!readyForInput())
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				serializationTreeViewer.getTree().setEnabled(state);
			}
		});
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
