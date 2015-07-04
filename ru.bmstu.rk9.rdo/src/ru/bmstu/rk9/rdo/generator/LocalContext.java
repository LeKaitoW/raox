package ru.bmstu.rk9.rdo.generator;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rdo.rdo.Pattern;
import ru.bmstu.rk9.rdo.rdo.RDOEnum;
import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ParameterType;
import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement;
import ru.bmstu.rk9.rdo.rdo.Function;
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic;
import ru.bmstu.rk9.rdo.rdo.FunctionList;
import ru.bmstu.rk9.rdo.rdo.FunctionParameter;
import ru.bmstu.rk9.rdo.rdo.Event;
import ru.bmstu.rk9.rdo.rdo.RelevantResource;
import ru.bmstu.rk9.rdo.rdo.GroupBy;
import ru.bmstu.rk9.rdo.generator.LocalContext;

public class LocalContext {
	public static class ContextEntry {
		public String type;
		public String generated;

		public ContextEntry(String generated, String type) {
			this.type = type;
			this.generated = generated;
		}
	}

	Map<String, ContextEntry> index;

	public LocalContext addRawEntry(String name, String type, String generated) {
		index.put(name, new ContextEntry(generated, type));
		return this;
	}

	public ContextEntry findEntry(String entry) {
		return index.get(entry);
	}

	public LocalContext() {
		index = new HashMap<String, ContextEntry>();
	}

	public LocalContext(LocalContext other) {
		if (other != null)
			index = new HashMap<String, ContextEntry>(other.index);
		else
			index = new HashMap<String, ContextEntry>();
	}

	public LocalContext populateWithEnums(RDOEnum enm) {
		for (String id : enm.getType().getValues())
			this.addRawEntry(enm.getType().getName() + "." + id,
					RDOExpressionCompiler.compileType(enm),
					RDOExpressionCompiler.compileType(enm) + "." + id);

		return this;
	}

	public LocalContext populateFromFunction(FunctionAlgorithmic function) {
		if (function.getParameters() != null)
			for (FunctionParameter parameter : function.getParameters())
				if (RDOExpressionCompiler.compileType(parameter.getType())
						.endsWith("_enum"))
					this.addRawEntry(parameter.getName(), RDOExpressionCompiler
							.compileType(parameter.getType()), parameter
							.getName());
				else
					this.addRawEntry(parameter.getName(), RDOExpressionCompiler
							.compileTypePrimitive(parameter.getType()),
							parameter.getName());

		if (RDOExpressionCompiler.compileType(
				((Function) function.eContainer()).getReturnType()).endsWith(
				"_enum"))
			this.populateWithEnums((RDOEnum) (RDOExpressionCompiler
					.resolveAllTypes(((Function) function.eContainer())
							.getReturnType())));

		return this;
	}

	public LocalContext populateFromFunction(FunctionList function) {
		if (function.getParameters() != null)
			for (FunctionParameter parameter : function.getParameters())
				if (RDOExpressionCompiler.compileType(parameter.getType())
						.endsWith("_enum"))
					this.addRawEntry(parameter.getName(), RDOExpressionCompiler
							.compileType(parameter.getType()), parameter
							.getName());
				else
					this.addRawEntry(parameter.getName(), RDOExpressionCompiler
							.compileTypePrimitive(parameter.getType()),
							parameter.getName());

		if (RDOExpressionCompiler.compileType(
				((Function) function.eContainer()).getReturnType()).endsWith(
				"_enum"))
			this.populateWithEnums((RDOEnum) (RDOExpressionCompiler
					.resolveAllTypes(((Function) function.eContainer())
							.getReturnType())));

		return this;
	}

	public LocalContext populateFromGroupBy(GroupBy groupBy) {
		return this.populateWithResourceRename(groupBy.getType(), "current");
	}

	public LocalContext populateWithResourceRename(ResourceType resourceType,
			String newName) {
		for (ParameterType parameter : resourceType.getParameters()) {
			this.addRawEntry(
					resourceType.getName() + "." + parameter.getName(),
					RDOExpressionCompiler.compileType(parameter), newName
							+ ".get_" + parameter.getName() + "()");
			this.addRawEntry(parameter.getName(),
					RDOExpressionCompiler.compileType(parameter), newName
							+ ".get_" + parameter.getName() + "()");
		}
		return this;
	}

	public LocalContext populateFromEvent(Event event) {
		for (ParameterType parameter : event.getParameters())
			this.addRawEntry(parameter.getName(),
					RDOExpressionCompiler.compileType(parameter), "parameters."
							+ parameter.getName());

		return this;
	}

	public LocalContext populateFromPattern(Pattern pattern) {
		for (ParameterType parameter : pattern.getParameters())
			this.addRawEntry(parameter.getName(),
					RDOExpressionCompiler.compileType(parameter), "parameters."
							+ parameter.getName());

		for (RelevantResource relevantResource : pattern.getRelevantResources()) {
			ResourceType type;
			if (relevantResource.getType() instanceof ResourceType)
				type = ((ResourceType) relevantResource.getType());
			else
				type = ((ResourceCreateStatement) relevantResource.getType())
						.getType();

			for (ParameterType parameter : type.getParameters())
				this.addRawEntry(
						relevantResource.getName() + "." + parameter.getName(),
						RDOExpressionCompiler.compileType(parameter),
						"resources." + relevantResource.getName() + ".get_"
								+ parameter.getName() + "()");
		}

		return this;
	}

	public void addCreatedResource(
			ResourceCreateStatement resourceCreateStatement) {
		for (ParameterType parameter : resourceCreateStatement.getType()
				.getParameters())
			this.addRawEntry(
					resourceCreateStatement.getName() + "."
							+ parameter.getName(),
					RDOExpressionCompiler.compileType(parameter),
					resourceCreateStatement.getName() + ".get_"
							+ parameter.getName() + "()");
	}
}
