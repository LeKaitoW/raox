package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;

import ru.bmstu.rk9.rao.ui.process.advance.Advance;
import ru.bmstu.rk9.rao.ui.process.generate.Generate;
import ru.bmstu.rk9.rao.ui.process.model.Model;
import ru.bmstu.rk9.rao.ui.process.release.Release;
import ru.bmstu.rk9.rao.ui.process.resource.Resource;
import ru.bmstu.rk9.rao.ui.process.seize.Seize;
import ru.bmstu.rk9.rao.ui.process.terminate.Terminate;

public class ProcessEditor extends GraphicalEditorWithFlyoutPalette {

	public ProcessEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public static final String ID = "ru.bmstu.rk9.rao.ui.process.editor";

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

		PaletteGroup processGroup = new PaletteGroup("Process");
		root.add(processGroup);

		processGroup.add(new CreationToolEntry("Generate", "Generate",
				new NodeCreationFactory(Generate.class), null, null));
		processGroup.add(new CreationToolEntry("Terminate", "Terminate",
				new NodeCreationFactory(Terminate.class), null, null));
		processGroup.add(new CreationToolEntry("Seize", "Seize",
				new NodeCreationFactory(Seize.class), null, null));
		processGroup.add(new CreationToolEntry("Release", "Release",
				new NodeCreationFactory(Release.class), null, null));
		processGroup.add(new CreationToolEntry("Advance", "Advance",
				new NodeCreationFactory(Advance.class), null, null));
		processGroup.add(new CreationToolEntry("Resource", "Resource",
				new NodeCreationFactory(Resource.class), null, null));

		root.setDefaultEntry(selectionToolEntry);
		return root;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	public Model createModel() {
		Model model = new Model();
		model.setLayout(new Rectangle(20, 20, 500, 500));
		Generate generate = new Generate();

		model.addChild(generate);
		generate.setLayout(new Rectangle(30, 50, 250, 150));
		return model;
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ProcessEditPartFactory());
	}

	protected void initializeGraphicalViewer() {
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setContents(createModel());
	}
}
