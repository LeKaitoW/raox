package ru.bmstu.rk9.rao.ui.serialization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.rao.RaoModel;
import ru.bmstu.rk9.rao.ui.build.ModelBuilder;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfig.SerializationNode;

import com.google.inject.Inject;

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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceChangeListener);

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

	IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						@Override
						public boolean visit(IResourceDelta delta)
								throws CoreException {
							IResource resource = delta.getResource();
							switch (resource.getType()) {
							case IResource.ROOT:
							case IResource.PROJECT:
							case IResource.FOLDER:
								return true;
							case IResource.FILE:
								handleFileChanges(delta);
								break;
							}
							return false;
						}

						private final void handleFileChanges(IResourceDelta delta) {
							if (!(delta.getResource() instanceof IFile))
								return;
							switch (delta.getKind()) {
							case IResourceDelta.ADDED: {
								IProject project = delta.getResource()
										.getProject();
								if (!project.isAccessible())
									return;

								updateProjectContents(project);
								break;
							}
							case IResourceDelta.CHANGED: {
								IFile file = (IFile) delta.getResource();
								if (!file.isAccessible())
									return;

								if (!("rao".equalsIgnoreCase(file
										.getFileExtension())))
									return;

								updateFileContents(file);
								break;
							}
							case IResourceDelta.REMOVED: {
								IFile file = (IFile) delta.getResource();
								if (!file.isAccessible())
									return;

								if (!("rao".equalsIgnoreCase(file
										.getFileExtension())))
									return;

								removeFileFromTree(file);
								break;
							}
							default:
								break;
							}
						}
					});
				} catch (CoreException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};

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

	private final Map<IResource, SerializationNode> modelNodes = new HashMap<>();
	private final Map<IProject, Set<IEditorPart>> projectReferences = new HashMap<>();

	private final void registerEditor(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (input == null)
			return;

		IResource resource = ResourceUtil.getFile(editor.getEditorInput());
		IProject project = resource.getProject();

		updateProjectContents(project);

		Set<IEditorPart> projectEditors = projectReferences.get(project);
		if (projectEditors.contains(editor))
			return;

		projectEditors.add(editor);

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

	private final void updateProjectContents(IProject project) {
		if (!projectReferences.containsKey(project))
			projectReferences.put(project, new HashSet<IEditorPart>());

		final List<IResource> projectFiles = ModelBuilder
				.getAllRaoFilesInProject(project);

		for (IResource raoFile : projectFiles) {
			updateFileContents((IFile) raoFile);
		}
	}

	private final void updateFileContents(IFile raoFile) {
		URI uri = ModelBuilder.getURI(raoFile);

		final ResourceSet resourceSet = resourceSetProvider.get(raoFile
				.getProject());
		Resource loadedResource = resourceSet.getResource(uri, true);
		EList<EObject> contents = loadedResource.getContents();

		if (contents.isEmpty())
			return;

		RaoModel model = (RaoModel) contents.get(0);

		if (modelNodes.containsKey(raoFile)) {
			updateInput(model.eResource());
			return;
		}

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

	private final void unregisterEditor(IEditorPart editor) {
		IProject unregisteredProject = removeEditorReference(editor);
		if (unregisteredProject == null)
			return;

		final List<IResource> projectFiles = ModelBuilder
				.getAllRaoFilesInProject(unregisteredProject);

		for (IResource raoFile : projectFiles)
			removeFileFromTree((IFile) raoFile);

		if (!serializationTreeViewer.getControl().isDisposed())
			serializationTreeViewer.refresh();
	}

	private final IProject removeEditorReference(IEditorPart editor) {
		Iterator<Entry<IProject, Set<IEditorPart>>> iterator = projectReferences
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<IProject, Set<IEditorPart>> entry = iterator.next();
			Set<IEditorPart> projectEditors = entry.getValue();
			if (!projectEditors.contains(editor))
				continue;

			projectEditors.remove(editor);
			if (projectEditors.isEmpty()) {
				IProject project = entry.getKey();
				iterator.remove();
				return project;
			}
		}
		return null;
	}

	private final void removeFileFromTree(IFile raoFile) {
		if (!modelNodes.containsKey(raoFile))
			return;

		SerializationNode modelNode = modelNodes.get(raoFile);
		serializationConfig.removeModel(modelNode);
		modelNodes.remove(raoFile);

		List<SerializationNode> modelsWithSameName = serializationConfig
				.findModelsWithSameName(modelNode.getFullName());
		if (modelsWithSameName.size() == 1)
			modelsWithSameName.get(0).mustShowFullName(false);
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
		PlatformUI
				.getWorkbench()
				.getDisplay()
				.asyncExec(
						() -> SerializationConfigView.serializationTreeViewer
								.refresh());
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
