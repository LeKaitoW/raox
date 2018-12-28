package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.runtime.IStatus;

public class ExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private IStatus status;

	public ExecutionException(IStatus status) {
		super(status.toString(), status.getException());
		this.status = status;
	}

	public IStatus getStatus() {
		return status;
	}

}
