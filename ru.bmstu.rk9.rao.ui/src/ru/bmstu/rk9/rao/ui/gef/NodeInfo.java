package ru.bmstu.rk9.rao.ui.gef;

import java.util.function.Supplier;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractEditPart;

public class NodeInfo {

	public NodeInfo(String name, Supplier<Node> nodeFactory, Supplier<AbstractEditPart> editPartFactory,
			Supplier<IFigure> figureFactory) {
		this.name = name;
		this.nodeFactory = nodeFactory;
		this.editPartFactory = editPartFactory;
		this.figureFactory = figureFactory;
	}

	private final String name;
	private final Supplier<Node> nodeFactory;
	private final Supplier<AbstractEditPart> editPartFactory;
	private final Supplier<IFigure> figureFactory;

	public final String getName() {
		return name;
	}

	public final Supplier<Node> getNodeFactory() {
		return nodeFactory;
	}

	public final Supplier<AbstractEditPart> getEditPartFactory() {
		return editPartFactory;
	}

	public final Supplier<IFigure> getFigureFactory() {
		return figureFactory;
	}
}
