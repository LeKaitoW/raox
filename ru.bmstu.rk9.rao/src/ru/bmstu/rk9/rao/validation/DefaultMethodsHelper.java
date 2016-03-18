package ru.bmstu.rk9.rao.validation;

public class DefaultMethodsHelper {
	public static enum ValidatorAction {
		ERROR, WARNING, NOTHING
	};

	public static class MethodInfo {
		MethodInfo(ValidatorAction action) {
			this.action = action;
		}

		int count = 0;
		ValidatorAction action;
	}

	public static enum GlobalMethodInfo {
		INIT("init", ValidatorAction.NOTHING), TERMINATE_CONDITION("terminateCondition", ValidatorAction.NOTHING);

		GlobalMethodInfo(String name, ValidatorAction validatorAction) {
			this.name = name;
			this.validatorAction = validatorAction;
		}

		final String name;
		final ValidatorAction validatorAction;
	}

	public static enum OperationMethodInfo {
		BEGIN("begin", ValidatorAction.NOTHING), END("end", ValidatorAction.NOTHING), DURATION("duration",
				ValidatorAction.NOTHING);

		OperationMethodInfo(String name, ValidatorAction validatorAction) {
			this.name = name;
			this.validatorAction = validatorAction;
		}

		final String name;
		final ValidatorAction validatorAction;
	}

	public static enum RuleMethodInfo {
		EXECUTE("execute", ValidatorAction.NOTHING);

		RuleMethodInfo(String name, ValidatorAction validatorAction) {
			this.name = name;
			this.validatorAction = validatorAction;
		}

		final String name;
		final ValidatorAction validatorAction;
	}

	public static enum DptMethodInfo {
		INIT("init", ValidatorAction.NOTHING);

		DptMethodInfo(String name, ValidatorAction validatorAction) {
			this.name = name;
			this.validatorAction = validatorAction;
		}

		final String name;
		final ValidatorAction validatorAction;
	}
}
