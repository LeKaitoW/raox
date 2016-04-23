package ru.bmstu.rk9.rao.ui.process;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import com.google.inject.Inject;

import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionCreationFactory;
import ru.bmstu.rk9.rao.ui.process.generate.GenerateEditPart;
import ru.bmstu.rk9.rao.ui.process.generate.GenerateNode;
import ru.bmstu.rk9.rao.ui.process.hold.HoldEditPart;
import ru.bmstu.rk9.rao.ui.process.hold.HoldNode;
import ru.bmstu.rk9.rao.ui.process.model.ModelEditPart;
import ru.bmstu.rk9.rao.ui.process.model.ModelLayer;
import ru.bmstu.rk9.rao.ui.process.model.ModelNode;
import ru.bmstu.rk9.rao.ui.process.node.BlockEditPart;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;
import ru.bmstu.rk9.rao.ui.process.node.BlockTitleEditPart;
import ru.bmstu.rk9.rao.ui.process.node.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.process.node.Node;
import ru.bmstu.rk9.rao.ui.process.node.NodeFactory;
import ru.bmstu.rk9.rao.ui.process.queue.QueueEditPart;
import ru.bmstu.rk9.rao.ui.process.queue.QueueNode;
import ru.bmstu.rk9.rao.ui.process.release.ReleaseEditPart;
import ru.bmstu.rk9.rao.ui.process.release.ReleaseNode;
import ru.bmstu.rk9.rao.ui.process.seize.SeizeEditPart;
import ru.bmstu.rk9.rao.ui.process.seize.SeizeNode;
import ru.bmstu.rk9.rao.ui.process.selectpath.SelectPathEditPart;
import ru.bmstu.rk9.rao.ui.process.selectpath.SelectPathNode;
import ru.bmstu.rk9.rao.ui.process.terminate.TerminateEditPart;
import ru.bmstu.rk9.rao.ui.process.terminate.TerminateNode;

public class ProcessEditor extends GraphicalEditorWithFlyoutPalette {

	public ProcessEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public static final String ID = "ru.bmstu.rk9.rao.ui.process.editor";
	public static final Map<Class<?>, NodeInfo> processNodesInfo = new LinkedHashMap<>();
	public static final String MODEL_LAYER = "Model Layer";
	private ModelNode model;

	@Inject
	IResourceSetProvider resourceSetProvider;

	static {
		processNodesInfo.put(ModelNode.class,
				new NodeInfo(ModelNode.name, () -> new ModelNode(), () -> new ModelEditPart()));
		processNodesInfo.put(GenerateNode.class,
				new NodeInfo(GenerateNode.name, () -> new GenerateNode(), () -> new GenerateEditPart()));
		processNodesInfo.put(TerminateNode.class,
				new NodeInfo(TerminateNode.name, () -> new TerminateNode(), () -> new TerminateEditPart()));
		processNodesInfo.put(SeizeNode.class,
				new NodeInfo(SeizeNode.name, () -> new SeizeNode(), () -> new SeizeEditPart()));
		processNodesInfo.put(ReleaseNode.class,
				new NodeInfo(ReleaseNode.name, () -> new ReleaseNode(), () -> new ReleaseEditPart()));
		processNodesInfo.put(HoldNode.class,
				new NodeInfo(HoldNode.name, () -> new HoldNode(), () -> new HoldEditPart()));
		processNodesInfo.put(QueueNode.class,
				new NodeInfo(QueueNode.name, () -> new QueueNode(), () -> new QueueEditPart()));
		processNodesInfo.put(SelectPathNode.class,
				new NodeInfo(SelectPathNode.name, () -> new SelectPathNode(), () -> new SelectPathEditPart()));
		processNodesInfo.put(BlockTitleNode.class,
				new NodeInfo("", () -> new BlockTitleNode(), () -> new BlockTitleEditPart()));
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();

		PaletteGroup selectGroup = new PaletteGroup("Selection");
		root.add(selectGroup);

		SelectionToolEntry selectionToolEntry = new SelectionToolEntry();
		selectGroup.add(selectionToolEntry);
		selectGroup.add(new MarqueeToolEntry());

		PaletteSeparator separator = new PaletteSeparator();
		root.add(separator);

		PaletteGroup connectionGroup = new PaletteGroup("Connection");
		root.add(connectionGroup);
		ConnectionCreationToolEntry connections = new ConnectionCreationToolEntry("Connection", "Create Connections",
				new ConnectionCreationFactory(), null, null);
		connectionGroup.add(connections);
		root.add(separator);

		PaletteGroup processGroup = new PaletteGroup("Process");
		root.add(processGroup);

		for (Class<?> nodeClass : processNodesInfo.keySet()) {
			if (!BlockNode.class.isAssignableFrom(nodeClass))
				continue;

			String nodeName = processNodesInfo.get(nodeClass).getName();
			processGroup
					.add(new CombinedTemplateCreationEntry(nodeName, nodeName, new NodeFactory(nodeClass), null, null));
		}
		root.setDefaultEntry(selectionToolEntry);
		getPalettePreferences().setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		return root;
	}

	private void setModel(ModelNode model) {
		this.model = model;
		model.setResourceRetriever(new EResourceRetriever(resourceSetProvider,
				((IFileEditorInput) getEditorInput()).getFile().getProject()));
	}

	public ModelNode getModel() {
		return model;
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		setPartName(file.getName());
		try {
			setModel(readModelFromFile(file));

			if (getGraphicalViewer() != null)
				getGraphicalViewer().setContents(getModel());
		} catch (EOFException e) {
			// http://stackoverflow.com/a/18451336
		} catch (Exception e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Failed to open",
					"Invalid file format");
		}
	};

	protected void writeToOutputStream(OutputStream outputStream) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(getModel());
		objectOutputStream.close();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		writeModelToFile(monitor);
		validateModel();
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ProcessEditPartFactory());

		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		viewer.setKeyHandler(keyHandler);

		getGraphicalViewer().setRootEditPart(new ScalableRootEditPart() {
			@Override
			protected LayeredPane createPrintableLayers() {
				LayeredPane pane = new LayeredPane();

				pane.add(new ModelLayer(), MODEL_LAYER);

				ConnectionLayer connectionLayer = new ConnectionLayer();
				connectionLayer.setAntialias(SWT.ON);
				pane.add(connectionLayer, CONNECTION_LAYER);

				Layer primaryLayer = new Layer();
				primaryLayer.setLayoutManager(new StackLayout());
				pane.add(primaryLayer, PRIMARY_LAYER);

				return pane;
			}
		});
	}

	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		if (model == null)
			setModel(new ModelNode());
		viewer.setContents(model);
		viewer.addDropTargetListener(new ProcessDropTargetListener(viewer));
		IViewSite site = null;
		try {
			site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.ProblemView").getViewSite();
		} catch (PartInitException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Failed to open",
					"Failed to open problem view");
			e.printStackTrace();
			return;
		}
		site.getWorkbenchWindow().getSelectionService().addSelectionListener(new ISelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (!(selection instanceof IStructuredSelection))
					return;

				IStructuredSelection structedSelection = (IStructuredSelection) selection;
				if (!(structedSelection.getFirstElement() instanceof MarkerItem))
					return;

				MarkerItem marker = (MarkerItem) structedSelection.getFirstElement();
				if (marker == null || marker.getMarker() == null)
					return;

				if (!marker.getMarker().getResource()
						.equals(((IFileEditorInput) ProcessEditor.this.getEditorInput()).getFile()))
					return;

				int nodeID = marker.getAttributeValue(Node.NODE_MARKER, 0);
				ModelEditPart modelPart = (ModelEditPart) getGraphicalViewer().getRootEditPart().getChildren().get(0);
				List<BlockEditPart> editParts = modelPart.getChildren();
				for (BlockEditPart editPart : editParts) {
					if (editPart.getID() == nodeID) {
						viewer.select(editPart);
						viewer.reveal(editPart);
						break;
					}
				}

			}
		});
	}

	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			@Override
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}

	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	public static ModelNode readModelFromFile(IFile file) throws ClassNotFoundException, IOException, CoreException {
		InputStream inputStream = file.getContents(false);
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		ModelNode model = (ModelNode) objectInputStream.readObject();
		objectInputStream.close();
		return model;
	}

	private void writeModelToFile(IProgressMonitor monitor) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				writeToOutputStream(outputStream);
				IFile file = ((IFileEditorInput) getEditorInput()).getFile();
				file.setContents(new ByteArrayInputStream(outputStream.toByteArray()), true, false, monitor);
				getCommandStack().markSaveLocation();
			}
		});
	}

	private void validateModel() {
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();
		try {
			file.deleteMarkers(BlockNode.PROCESS_PROBLEM_MARKER, true, IResource.DEPTH_ZERO);
			for (ru.bmstu.rk9.rao.ui.gef.Node node : model.getChildren()) {
				if (node instanceof BlockNode) {
					((BlockNode) node).validateProperty(file);
				}
			}
		} catch (CoreException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Internal error",
					"Internal error during problem markers creation");
		}
	}
}
