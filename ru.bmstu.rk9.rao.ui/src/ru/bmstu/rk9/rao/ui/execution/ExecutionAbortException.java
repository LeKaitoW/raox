package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.runtime.IStatus;

public class ExecutionAbortException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private IStatus status;

	public ExecutionAbortException(IStatus status) {
		this.status = status;
	}

	public IStatus getStatus() {
		return status;
	}
}
