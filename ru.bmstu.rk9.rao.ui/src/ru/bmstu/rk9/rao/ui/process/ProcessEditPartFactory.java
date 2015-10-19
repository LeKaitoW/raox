package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

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

public class ProcessEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;

		if (model instanceof Model) {
			part = new ModelPart();
		} else if (model instanceof Generate) {
			part = new GeneratePart();
		} else if (model instanceof Advance) {
			part = new AdvancePart();
		} else if (model instanceof Release) {
			part = new ReleasePart();
		} else if (model instanceof Resource) {
			part = new ResourcePart();
		} else if (model instanceof Seize) {
			part = new SeizePart();
		} else if (model instanceof Terminate) {
			part = new TerminatePart();
		}

		part.setModel(model);
		return part;
	}
}
