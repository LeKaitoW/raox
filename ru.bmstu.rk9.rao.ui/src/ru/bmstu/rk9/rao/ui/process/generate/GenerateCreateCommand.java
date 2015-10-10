package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.model.Model;

public class GenerateCreateCommand extends Command {

	private Model model;
	private Generate generate;

	public GenerateCreateCommand() {
		super();
		model = null;
		generate = null;
	}

}
