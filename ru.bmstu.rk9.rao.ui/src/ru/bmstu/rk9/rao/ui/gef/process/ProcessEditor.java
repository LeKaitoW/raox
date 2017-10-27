package ru.bmstu.rk9.rao.ui.gef.process;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.GuideLayer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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

import ru.bmstu.rk9.rao.ui.gef.AntialiasedLayer;
import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.model.ModelBackgroundLayer;
import ru.bmstu.rk9.rao.ui.gef.model.ModelLayer;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNodeFactory;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.generate.GenerateEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.generate.GenerateFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.generate.GenerateNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.hold.HoldEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.hold.HoldFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.hold.HoldNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.queue.QueueEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.queue.QueueFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.queue.QueueNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.release.ReleaseEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.release.ReleaseFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.release.ReleaseNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.seize.SeizeEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.seize.SeizeFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.seize.SeizeNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.selectpath.SelectPathEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.selectpath.SelectPathFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.selectpath.SelectPathNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.terminate.TerminateEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.terminate.TerminateFigure;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.terminate.TerminateNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionCreationFactory;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelNode;

public class ProcessEditor extends GraphicalEditorWithFlyoutPalette {

	class FeedbackLayer extends AntialiasedLayer {
		FeedbackLayer() {
			setEnabled(false);
		}

		@Override
		public Dimension getPreferredSize(int wHint, int hHint) {
			Rectangle rectangle = new Rectangle();
			for (int i = 0; i < getChildren().size(); i++)
				rectangle.union(((IFigure) getChildren().get(i)).getBounds());
			return rectangle.getSize();
		}
	}

	public ProcessEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public static final String ID = "ru.bmstu.rk9.rao.ui.gef.process.editor";
	private static final Map<Class<? extends Node>, NodeInfo> nodesInfo = new LinkedHashMap<>();
	private static final Map<Class<? extends EditPart>, NodeInfo> nodesInfoByEditPart = new LinkedHashMap<>();
	private ProcessModelNode model;

	@Inject
	IResourceSetProvider resourceSetProvider;

	static {
		addNodeInfo(ProcessModelNode.class, ProcessModelEditPart.class, ModelLayer.class);
		addNodeInfo(GenerateNode.class, GenerateEditPart.class, GenerateFigure.class);
		addNodeInfo(TerminateNode.class, TerminateEditPart.class, TerminateFigure.class);
		addNodeInfo(SeizeNode.class, SeizeEditPart.class, SeizeFigure.class);
		addNodeInfo(ReleaseNode.class, ReleaseEditPart.class, ReleaseFigure.class);
		addNodeInfo(HoldNode.class, HoldEditPart.class, HoldFigure.class);
		addNodeInfo(QueueNode.class, QueueEditPart.class, QueueFigure.class);
		addNodeInfo(SelectPathNode.class, SelectPathEditPart.class, SelectPathFigure.class);
		addNodeInfo(BlockTitleNode.class, BlockTitleEditPart.class, BlockTitleFigure.class);
	}

	public static boolean hasNodeInfo(Class<? extends Node> node) {
		return getNodeInfo(node) != null;
	}

	public static NodeInfo getNodeInfo(Class<? extends Node> node) {
		return nodesInfo.get(node);
	}

	public static NodeInfo getNodeInfoByEditPart(Class<? extends EditPart> editPart) {
		return nodesInfoByEditPart.get(editPart);
	}

	private static void addNodeInfo(Class<? extends Node> node, Class<? extends EditPart> editPart,
			Class<? extends Figure> figure) {
		try {
			NodeInfo nodeInfo = new NodeInfo(node.getField("name").get(null).toString(), () -> createObject(node),
					() -> createObject(editPart), () -> createObject(figure));
			nodesInfo.put(node, nodeInfo);
			nodesInfoByEditPart.put(editPart, nodeInfo);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static <T> T createObject(Class<T> node) {
		try {
			Constructor<T> constructor = node.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();

		PaletteGroup paletteGroup = new PaletteGroup("Selection");
		root.add(paletteGroup);

		PanningSelectionToolEntry panningSelectionToolEntry = new PanningSelectionToolEntry();
		panningSelectionToolEntry.setToolClass(ProcessSelectionTool.class);
		paletteGroup.add(panningSelectionToolEntry);
		paletteGroup.add(new MarqueeToolEntry());

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

		for (Map.Entry<Class<? extends Node>, NodeInfo> node : nodesInfo.entrySet()) {
			if (!BlockNode.class.isAssignableFrom(node.getKey()))
				continue;

			final String nodeName = node.getValue().getName();

			BlockFigure blockFigure = (BlockFigure) node.getValue().getFigureFactory().get();
			processGroup.add(new CombinedTemplateCreationEntry(nodeName, nodeName, new BlockNodeFactory(node.getKey()),
					ImageDescriptor.createFromImageData(blockFigure.getSmallPreviewImageData()),
					ImageDescriptor.createFromImageData(blockFigure.getLargePreviewImageData())));
		}
		root.setDefaultEntry(panningSelectionToolEntry);
		getPalettePreferences().setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		return root;
	}

	private void setModel(ProcessModelNode model) {
		this.model = model;
		model.setResourceRetriever(new EResourceRetriever(resourceSetProvider,
				((IFileEditorInput) getEditorInput()).getFile().getProject()));
	}

	public ProcessModelNode getModel() {
		return model;
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		setPartName(file.getName());
		try {
			ProcessModelNode model = readModelFromFile(file);

			if (model == null)
				return;
			setModel(model);

			if (getGraphicalViewer() != null)
				getGraphicalViewer().setContents(getModel());
		} catch (IOException | CoreException | ClassNotFoundException e) {
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

		getGraphicalViewer().setRootEditPart(new ScalableFreeformRootEditPart() {

			@Override
			protected void createLayers(LayeredPane layeredPane) {
				layeredPane.add(getScaledLayers(), SCALABLE_LAYERS);
				layeredPane.add(new FreeformLayer() {
					@Override
					public Dimension getPreferredSize(int wHint, int hHint) {
						return new Dimension();
					}
				}, HANDLE_LAYER);
				layeredPane.add(new FeedbackLayer(), FEEDBACK_LAYER);
				layeredPane.add(new GuideLayer(), GUIDE_LAYER);
			}

			@Override
			protected ScalableFreeformLayeredPane createScaledLayers() {
				ScalableFreeformLayeredPane layers = new ScalableFreeformLayeredPane();
				layers.add(new ModelBackgroundLayer(), ModelBackgroundLayer.MODEL_BACKGROUND_LAYER);
				layers.add(createGridLayer(), GRID_LAYER);
				layers.add(getPrintableLayers(), PRINTABLE_LAYERS);
				return layers;
			}

			@Override
			protected GridLayer createGridLayer() {
				return new ProcessGridLayer();
			}

			@Override
			protected LayeredPane createPrintableLayers() {
				LayeredPane layers = new ScalableFreeformLayeredPane();

				ConnectionLayer connectionLayer = new ConnectionLayer();
				connectionLayer.setAntialias(SWT.ON);
				layers.add(connectionLayer, CONNECTION_LAYER);

				Layer primaryLayer = new AntialiasedLayer();
				primaryLayer.setLayoutManager(new StackLayout());
				layers.add(primaryLayer, PRIMARY_LAYER);

				return layers;
			}
		});
	}

	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		if (model == null)
			setModel(new ProcessModelNode());
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

				int blockNodeID = marker.getAttributeValue(BlockNode.BLOCK_NODE_MARKER, 0);
				ProcessModelEditPart modelPart = (ProcessModelEditPart) getGraphicalViewer().getRootEditPart()
						.getChildren().get(0);
				List<EditPart> editParts = modelPart.getChildren();
				for (EditPart editPart : editParts) {
					if (!(editPart instanceof BlockEditPart))
						continue;

					if (((BlockEditPart) editPart).getID() == blockNodeID) {
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

	public static ProcessModelNode readModelFromFile(IFile file)
			throws ClassNotFoundException, IOException, CoreException {
		ProcessModelNode model;
		try (ObjectInputStream objectInputStream = new ObjectInputStream(file.getContents(false))) {
			model = (ProcessModelNode) objectInputStream.readObject();
		} catch (EOFException e) {
			model = null;
		}
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
			for (Node node : model.getChildren()) {
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
