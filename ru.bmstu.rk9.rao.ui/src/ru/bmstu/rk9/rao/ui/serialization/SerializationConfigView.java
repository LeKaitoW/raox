package ru.bmstu.rk9.rao.ui.serialization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.ui.execution.BuildUtil;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfig.SerializationNode;

import com.google.inject.Inject;

public class SerializationConfigView extends ViewPart {
	public static final String ID = "ru.bmstu.rk9.rao.ui.SerializationConfigView";

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------------------- API ------------------------------ //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	public final void setCheckedStateForAll() {
		setStateForAll(true);
	}

	public final void setUncheckedStateForAll() {
		setStateForAll(false);
	}

	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------------------- API HELPERS --------------------------- //
	// ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	private final void setStateForAll(boolean state) {
		if (!readyForInput())
			return;

		SerializationConfig serializationConfig = (SerializationConfig) serializationTreeViewer.getInput();
		if (serializationConfig == null)
			return;

		SerializationNode root = serializationConfig.getRoot();

		for (SerializationNode node : root.getVisibleChildren()) {
			node.setSerializationState(state);
			serializationTreeViewer.setSubtreeChecked(node, state);
			node.setSerializeVisibleChildren(state);
		}

		serializationTreeViewer.refresh();
		return;
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

		serializationTreeViewer.setContentProvider(new RaoSerializationConfigContentProvider());
		serializationTreeViewer.setLabelProvider(new RaoSerializationConfigLabelProvider());
		serializationTreeViewer.setCheckStateProvider(new RaoSerializationConfigCheckStateProvider());

		serializationTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				boolean serializationState = event.getChecked();
				SerializationNode node = (SerializationNode) event.getElement();

				node.setSerializationState(serializationState);
				serializationTreeViewer.setSubtreeChecked(node, serializationState);
				node.setSerializeVisibleChildren(serializationState);
			}
		});

		serializationTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object item = serializationTreeViewer.getTree().getSelection()[0].getData();
				if (item == null)
					return;

				if (serializationTreeViewer.getExpandedState(item))
					serializationTreeViewer.collapseToLevel(item, 1);
				else
					serializationTreeViewer.expandToLevel(item, 1);
			}
		});

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);

		initializeSubscribers();
		initializeTree();
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
		return serializationTreeViewer != null && !serializationTreeViewer.getTree().isDisposed()
				&& serializationTreeViewer.getContentProvider() != null
				&& serializationTreeViewer.getLabelProvider() != null;
	}

	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ---------------------- TREE STRUCTURE UPDATE ------------------------- //
	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

	@Inject
	IResourceSetProvider resourceSetProvider;

	private final Map<IProject, Map<IResource, SerializationNode>> openedProjectsContents = new ConcurrentHashMap<>();

	private final void initializeTree() {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			addProjectToTree(project);
	}

	IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				try {
					event.getDelta().accept(serializationTreeDeltaVisitor);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

	private final IResourceDeltaVisitor serializationTreeDeltaVisitor = new IResourceDeltaVisitor() {
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (resource.getType()) {
			case IResource.ROOT:
			case IResource.FOLDER:
				return true;
			case IResource.PROJECT:
				handleProjectChanges((IProject) delta.getResource(), delta.getKind());
				return true;
			case IResource.FILE:
				handleFileChanges((IFile) delta.getResource(), delta.getKind());
				break;
			}
			return false;
		}

		private final void handleProjectChanges(IProject project, int deltaKind) {
			switch (deltaKind) {
			case IResourceDelta.ADDED:
				addProjectToTree(project);
				break;
			case IResourceDelta.REMOVED:
				removeProjectFromTree(project);
				break;
			}
			return;
		}

		private final void handleFileChanges(IFile file, int deltaKind) {
			switch (deltaKind) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED: {
				if (!file.isAccessible()) {
					return;
				}

				if (!("rao".equalsIgnoreCase(file.getFileExtension()))) {
					return;
				}

				updateFileContents(file);
				break;
			}
			case IResourceDelta.REMOVED: {
				if (!("rao".equalsIgnoreCase(file.getFileExtension())))
					return;

				removeFileFromTree(file);
				break;
			}
			default:
				break;
			}
		}
	};

	private final void addProjectToTree(IProject project) {
		openedProjectsContents.put(project, new ConcurrentHashMap<IResource, SerializationNode>());

		final List<IResource> projectFiles = BuildUtil.getAllRaoFilesInProject(project);

		for (IResource raoFile : projectFiles) {
			updateFileContents((IFile) raoFile);
		}
	}

	private final void updateFileContents(IFile raoFile) {
		URI uri = BuildUtil.getURI(raoFile);
		IProject project = raoFile.getProject();

		final ResourceSet resourceSet = resourceSetProvider.get(project);
		if (resourceSet == null)
			return;

		Resource loadedResource = resourceSet.getResource(uri, true);
		if (loadedResource == null)
			return;

		EList<EObject> contents = loadedResource.getContents();
		if (contents.isEmpty())
			return;

		RaoModel model = (RaoModel) contents.get(0);

		if (openedProjectsContents.get(project).containsKey(raoFile)) {
			updateInput(model.eResource());
			return;
		}

		SerializationNode newModel = addModel(model.eResource());
		openedProjectsContents.get(project).put(raoFile, newModel);

		List<SerializationNode> modelsWithSameName = serializationConfig.findModelsWithSameName(newModel.getFullName());
		if (modelsWithSameName.size() > 1)
			for (SerializationNode node : modelsWithSameName)
				node.mustShowFullName(true);

		if (serializationTreeViewer.getInput() == null)
			serializationTreeViewer.setInput(serializationConfig);

		PlatformUI.getWorkbench().getDisplay()
				.asyncExec(() -> SerializationConfigView.serializationTreeViewer.refresh());
	}

	private final void removeProjectFromTree(IProject project) {
		for (IResource raoFile : openedProjectsContents.get(project).keySet()) {
			removeFileFromTree((IFile) raoFile);
		}

		openedProjectsContents.remove(project);
	}

	private final void removeFileFromTree(IFile raoFile) {
		IProject project = raoFile.getProject();

		if (!openedProjectsContents.containsKey(project))
			return;

		SerializationNode modelNode = openedProjectsContents.get(project).get(raoFile);
		serializationConfig.removeModel(modelNode);
		openedProjectsContents.get(project).remove(raoFile);

		List<SerializationNode> modelsWithSameName = serializationConfig
				.findModelsWithSameName(modelNode.getFullName());
		if (modelsWithSameName.size() == 1)
			modelsWithSameName.get(0).mustShowFullName(false);

		PlatformUI.getWorkbench().getDisplay()
				.asyncExec(() -> SerializationConfigView.serializationTreeViewer.refresh());
	}

	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //
	// ----------------- REAL TIME MODEL CONTENTS UPDATE -------------------- //
	// ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――― //

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
			registerEditor(editor);
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
			unregisterEditor(editor);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
		}
	};

	private final Set<IEditorPart> registeredEditors = new HashSet<>();

	private final void registerEditor(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input == null)
			return;

		if (registeredEditors.contains(editor))
			return;

		if (!(editor instanceof XtextEditor))
			return;

		registeredEditors.add(editor);

		((XtextEditor) editor).getDocument().addModelListener(new IXtextModelListener() {
			@Override
			public void modelChanged(XtextResource resource) {
				updateInput(resource);
			}
		});

		return;
	}

	private final void unregisterEditor(IEditorPart editor) {
		registeredEditors.remove(editor);

		if (!serializationTreeViewer.getControl().isDisposed())
			serializationTreeViewer.refresh();
	}

	private static SerializationNode addModel(Resource model) {
		SerializationNode modelNode = serializationConfigurator.initModel(serializationConfig.getRoot(), model);
		updateInput(model);
		return modelNode;
	}

	private static void updateInput(Resource model) {
		if (!readyForInput())
			return;
		SerializationNode modelNode = serializationConfig.findModel(model.getURI().toPlatformString(false));
		serializationConfigurator.fillCategories(model, modelNode);
		PlatformUI.getWorkbench().getDisplay()
				.asyncExec(() -> SerializationConfigView.serializationTreeViewer.refresh());
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
		subscriberRegistrationManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(enableSubscriber, ExecutionState.EXECUTION_COMPLETED),
						new SimulatorSubscriberInfo(disableSubscriber, ExecutionState.EXECUTION_STARTED)));
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
		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> serializationTreeViewer.getTree().setEnabled(state));
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
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		SerializationConfig serializationConfig = (SerializationConfig) inputElement;
		if (!serializationConfig.getRoot().hasChildren())
			return new Object[] {};
		return serializationConfig.getRoot().getVisibleChildren().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		SerializationNode serializationNode = (SerializationNode) parentElement;
		if (!serializationNode.hasChildren())
			return new Object[] {};
		return serializationNode.getVisibleChildren().toArray();
	}

	@Override
	public Object getParent(Object element) {
		SerializationNode serializationNode = (SerializationNode) element;
		return serializationNode.getParent();
	}

	@Override
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
