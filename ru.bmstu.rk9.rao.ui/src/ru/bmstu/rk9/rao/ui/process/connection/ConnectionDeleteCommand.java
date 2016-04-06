package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

public class ConnectionDeleteCommand extends Command {

	private Connection connection;

	public void setConnection(Object model) {
		this.connection = (Connection) model;
	}

	@Override
	public boolean canExecute() {
		if (connection == null)
			return false;
		return true;
	}

	@Override
	public void execute() {
		connection.disconnect();
	}

	@Override
	public boolean canUndo() {
		if (connection == null)
			return false;
		return true;
	}

	@Override
	public void undo() {
		connection.connect();
	}
}
