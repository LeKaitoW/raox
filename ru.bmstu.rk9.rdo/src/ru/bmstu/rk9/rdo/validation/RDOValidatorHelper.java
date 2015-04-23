package ru.bmstu.rk9.rdo.validation;

public class RDOValidatorHelper {
	public static class DefaultMethodsHelper {
		public static enum GlobalMethods {
			INIT("init"), TERMINATE_CONDITION("terminateCondition");
			GlobalMethods(String name) {
				this.name = name;
			}
			final String name;
		}

		public static enum OperationMethods {
			BEGIN("begin"), END("end"), DURATION("duration");
			OperationMethods(String name) {
				this.name = name;
			}
			final String name;
		}

		public static enum EventOrRuleMethods {
			EXECUTE("execute");
			EventOrRuleMethods(String name) {
				this.name = name;
			}
			final String name;
		}
	}
}
