package ru.bmstu.rk9.rdo.generator;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bmstu.rk9.rdo.rdo.EnumerativeSequence;
import ru.bmstu.rk9.rdo.rdo.HistogramSequence;
import ru.bmstu.rk9.rdo.rdo.RegularSequence;
import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ParameterType;
import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement;
import ru.bmstu.rk9.rdo.rdo.Constant;
import ru.bmstu.rk9.rdo.rdo.Function;
import ru.bmstu.rk9.rdo.rdo.FunctionParameter;
import ru.bmstu.rk9.rdo.rdo.Sequence;
import ru.bmstu.rk9.rdo.rdo.SequenceType;

public class GlobalContext {
	public class ResourceTypeGlobalReference {
		public ResourceType origin;

		public HashMap<String, String> parameters;

		public ResourceTypeGlobalReference(ResourceType resourceType) {
			origin = resourceType;
			parameters = new HashMap<String, String>();
			for (ParameterType p : resourceType.getParameters())
				parameters.put(p.getName(),
						RDOExpressionCompiler.compileType(p));
		}
	}

	public class ResourceGlobalReference {
		public ResourceCreateStatement origin;

		public String type;

		public ResourceGlobalReference(
				ResourceCreateStatement resourceCreateStatement) {
			origin = resourceCreateStatement;
			ResourceType resourceType = resourceCreateStatement.getType();
			resourceTypes.put(resourceType.getName(),
					new ResourceTypeGlobalReference(resourceType));
			type = resourceType.getName();
		}
	}

	public class SequenceGlobalReference {
		public Sequence origin;

		public String type;
		public int parameters;

		public SequenceGlobalReference(Sequence sequence) {
			origin = sequence;
			type = RDOExpressionCompiler.compileType(sequence.getReturnType());
			SequenceType type = sequence.getType();
			if (type instanceof EnumerativeSequence
					|| type instanceof HistogramSequence)
				parameters = 0;
			else
				switch (((RegularSequence) type).getType()) {
				case "exponential":
					parameters = 1;
				case "normal":
					parameters = 2;
				case "triangular":
					parameters = 3;
				case "uniform":
					parameters = 2;
				}
		}
	}

	public class ConstantGlobalReference {
		public Constant origin;

		public String type;

		public ConstantGlobalReference(Constant constant) {
			origin = constant;
			type = RDOExpressionCompiler.compileType(constant.getType());
		}
	}

	public class FunctionGlobalReference {
		public Function origin;

		public LinkedList<String> parameters = new LinkedList<String>();

		public String type;

		public FunctionGlobalReference(Function function) {
			origin = function;

			List<FunctionParameter> parameters = null;

			type = RDOExpressionCompiler.compileType(function.getReturnType());

			parameters = function.getType().getParameters();

			if (parameters != null)
				for (FunctionParameter p : parameters)
					this.parameters.addLast(RDOExpressionCompiler.compileType(p
							.getType()));
		}
	}

	public ResourceGlobalReference newResourceReference(
			ResourceCreateStatement resourceCreateStatement) {
		return new ResourceGlobalReference(resourceCreateStatement);
	}

	public SequenceGlobalReference newSequenceReference(Sequence sequence) {
		return new SequenceGlobalReference(sequence);
	}

	public ConstantGlobalReference newConstantReference(Constant constant) {
		return new ConstantGlobalReference(constant);
	}

	public FunctionGlobalReference newFunctionReference(Function fun) {
		return new FunctionGlobalReference(fun);
	}

	public final Map<String, ResourceTypeGlobalReference> resourceTypes = new HashMap<String, ResourceTypeGlobalReference>();
	public final Map<String, ResourceGlobalReference> resources = new HashMap<String, ResourceGlobalReference>();
	public final Map<String, SequenceGlobalReference> sequences = new HashMap<String, SequenceGlobalReference>();
	public final Map<String, ConstantGlobalReference> constants = new HashMap<String, ConstantGlobalReference>();
	public final Map<String, FunctionGlobalReference> functions = new HashMap<String, FunctionGlobalReference>();
}
