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
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;

import ru.bmstu.rk9.rao.ui.process.advance.Advance;
import ru.bmstu.rk9.rao.ui.process.advance.AdvancePart;
import ru.bmstu.rk9.rao.ui.process.generate.Generate;
import ru.bmstu.rk9.rao.ui.process.generate.GeneratePart;
import ru.bmstu.rk9.rao.ui.process.model.Model;
import ru.bmstu.rk9.rao.ui.process.model.ModelPart;
import ru.bmstu.rk9.rao.ui.process.release.Release;
import ru.bmstu.rk9.rao.ui.process.release.ReleasePart;
import ru.bmstu.rk9.rao.ui.process.resource.Resource;
import ru.bmstu.rk9.rao.ui.process.resource.ResourcePart;
import ru.bmstu.rk9.rao.ui.process.seize.Seize;
import ru.bmstu.rk9.rao.ui.process.seize.SeizePart;
import ru.bmstu.rk9.rao.ui.process.terminate.Terminate;
import ru.bmstu.rk9.rao.ui.process.terminate.TerminatePart;

public class ProcessEditor extends GraphicalEditorWithFlyoutPalette {

	public ProcessEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public static final String ID = "ru.bmstu.rk9.rao.ui.process.editor";
	private Model model;
	public static final Map<Class<?>, ProcessNodeInfo> processNodesInfo = new LinkedHashMap<>();

	static {
		processNodesInfo.put(Model.class, new ProcessNodeInfo(Model.name, () -> new Model(), () -> new ModelPart()));
		processNodesInfo.put(Generate.class,
				new ProcessNodeInfo(Generate.name, () -> new Generate(), () -> new GeneratePart()));
		processNodesInfo.put(Terminate.class,
				new ProcessNodeInfo(Terminate.name, () -> new Terminate(), () -> new TerminatePart()));
		processNodesInfo.put(Seize.class, new ProcessNodeInfo(Seize.name, () -> new Seize(), () -> new SeizePart()));
		processNodesInfo.put(Release.class,
				new ProcessNodeInfo(Release.name, () -> new Release(), () -> new ReleasePart()));
		processNodesInfo.put(Advance.class,
				new ProcessNodeInfo(Advance.name, () -> new Advance(), () -> new AdvancePart()));
		processNodesInfo.put(Resource.class,
				new ProcessNodeInfo(Resource.name, () -> new Resource(), () -> new ResourcePart()));
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

		PaletteGroup linkGroup = new PaletteGroup("Link");
		root.add(linkGroup);
		ConnectionCreationToolEntry links = new ConnectionCreationToolEntry("Link", "Create Links",
				new LinkCreationFactory(), null, null);
		linkGroup.add(links);
		root.add(separator);

		PaletteGroup processGroup = new PaletteGroup("Process");
		root.add(processGroup);

		for (Class<?> nodeClass : processNodesInfo.keySet()) {
			String nodeName = processNodesInfo.get(nodeClass).getName();
			if (!nodeClass.equals(Model.class))
				processGroup.add(new CombinedTemplateCreationEntry(nodeName, nodeName,
						new NodeCreationFactory(nodeClass), null, null));
		}
		root.setDefaultEntry(selectionToolEntry);
		return root;
	}

	private void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();
		try {
			InputStream inputStream = file.getContents(false);
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			setModel((Model) objectInputStream.readObject());
			objectInputStream.close();

			if (getGraphicalViewer() != null)
				getGraphicalViewer().setContents(getModel());
		} catch (EOFException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	protected void writeToOutputStream(OutputStream outputStream) throws IOException {
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(getModel());
		objectOutputStream.close();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
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

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ProcessEditPartFactory());

		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		viewer.setKeyHandler(keyHandler);
	}

	@Override
	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		if (model == null)
			model = new Model();
		viewer.setContents(model);
		viewer.addDropTargetListener(new ProcessDropTargetListener(viewer));
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
}
