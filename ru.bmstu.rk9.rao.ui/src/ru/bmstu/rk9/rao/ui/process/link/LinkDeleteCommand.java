package ru.bmstu.rk9.rao.ui.process.link;

import org.eclipse.gef.commands.Command;

public class LinkDeleteCommand extends Command {

	private ProcessLink link;

	public void setLink(Object model) {
		this.link = (ProcessLink) model;
	}

	@Override
	public boolean canExecute() {
		if (link == null)
			return false;
		return true;
	}

	@Override
	public void execute() {
		link.disconnect();
	}

	@Override
	public boolean canUndo() {
		if (link == null)
			return false;
		return true;
	}

	@Override
	public void undo() {
		link.connect();
	}
}
