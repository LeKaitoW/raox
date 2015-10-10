package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.bmstu.rk9.rao.ui.process.generate.Generate;
import ru.bmstu.rk9.rao.ui.process.generate.GeneratePart;
import ru.bmstu.rk9.rao.ui.process.model.Model;
import ru.bmstu.rk9.rao.ui.process.model.ModelPart;

public class ProcessEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;

		if (model instanceof Model) {
			part = new ModelPart();
		} else if (model instanceof Generate) {
			part = new GeneratePart();
		}

		part.setModel(model);
		return part;
	}

}
