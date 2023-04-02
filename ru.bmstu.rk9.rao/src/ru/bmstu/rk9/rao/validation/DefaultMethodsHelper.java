package ru.bmstu.rk9.rao.validation;

public class DefaultMethodsHelper {
	public static enum ValidatorAction {
		ERROR, WARNING, NOTHING
	};

	public static class MethodInfo {
		MethodInfo(ValidatorAction action, String[] parameters) {
			this.action = action;
			this.parameters = parameters;
		}

		int count = 0;
		ValidatorAction action;
		String[] parameters;
	}

	public static interface AbstractMethodInfo {
		public String getName();

		public ValidatorAction getValidatorAction();

		public String[] getParameters();
	}

	public static enum GlobalMethodInfo implements AbstractMethodInfo {
		INIT("init", ValidatorAction.NOTHING, new String[] {}),
		TERMINATE_CONDITION("terminateCondition", ValidatorAction.NOTHING, new String[] {}),
		TIME_FORMAT("timeFormat", ValidatorAction.NOTHING, new String[] { "double time" }),
		TIME_START("timeStart", ValidatorAction.NOTHING, new String[] {});

		GlobalMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}

	public static enum OperationMethodInfo implements AbstractMethodInfo {
		BEGIN("begin", ValidatorAction.NOTHING, new String[] {}), END("end", ValidatorAction.NOTHING, new String[] {}),
		DURATION("duration", ValidatorAction.NOTHING, new String[] {});

		OperationMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}

	public static enum RuleMethodInfo implements AbstractMethodInfo {
		EXECUTE("execute", ValidatorAction.NOTHING, new String[] {});

		RuleMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}

	public static enum DptMethodInfo implements AbstractMethodInfo {
		INIT("init", ValidatorAction.NOTHING, new String[] {});

		DptMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}

	public static enum FrameMethodInfo implements AbstractMethodInfo {
		INIT("init", ValidatorAction.NOTHING, new String[] {}), //
		DRAW("draw", ValidatorAction.NOTHING, new String[] {}), //
		MOUSE_DOWN("mouseDown", ValidatorAction.NOTHING,
				new String[] { "int x", "int y", "int button", "int stateMask" }), //
		MOUSE_UP("mouseUp", ValidatorAction.NOTHING, new String[] { "int x", "int y", "int button", "int stateMask" }), //
		MOUSE_MOVE("mouseMove", ValidatorAction.NOTHING,
				new String[] { "int x", "int y", "int button", "int stateMask" }), //
		MOUSE_DOUBLECLICK("mouseDoubleClick", ValidatorAction.NOTHING,
				new String[] { "int x", "int y", "int button", "int stateMask" }), //
		MOUSE_SCROLLED("mouseScrolled", ValidatorAction.NOTHING, new String[] { "int x", "int y", "int count" }), //
		KEY_RELEASED("keyReleased", ValidatorAction.NOTHING, new String[] { "int keyCode", "int stateMask" }), //
		KEY_PRESSED("keyPressed", ValidatorAction.NOTHING, new String[] { "int keyCode", "int stateMask" });

		FrameMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}

	public static enum DataSourceMethodInfo implements AbstractMethodInfo {
		EVALUATE("evaluate", ValidatorAction.NOTHING, new String[] {}),
		CONDITION("condition", ValidatorAction.NOTHING, new String[] {});

		DataSourceMethodInfo(String name, ValidatorAction validatorAction, String[] parameters) {
			this.name = name;
			this.validatorAction = validatorAction;
			this.parameters = parameters;
		}

		public final String name;
		final ValidatorAction validatorAction;
		public final String[] parameters;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public ValidatorAction getValidatorAction() {
			return validatorAction;
		}

		@Override
		public String[] getParameters() {
			return parameters;
		}
	}
}
