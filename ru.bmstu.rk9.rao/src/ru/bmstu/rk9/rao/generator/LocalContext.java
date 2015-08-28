package ru.bmstu.rk9.rao.generator;

import java.util.HashMap;
import java.util.Map;

import ru.bmstu.rk9.rao.generator.LocalContext;
import ru.bmstu.rk9.rao.rao.Event;
import ru.bmstu.rk9.rao.rao.Function;
import ru.bmstu.rk9.rao.rao.FunctionAlgorithmic;
import ru.bmstu.rk9.rao.rao.FunctionList;
import ru.bmstu.rk9.rao.rao.FunctionParameter;
import ru.bmstu.rk9.rao.rao.GroupBy;
import ru.bmstu.rk9.rao.rao.Parameter;
import ru.bmstu.rk9.rao.rao.Pattern;
import ru.bmstu.rk9.rao.rao.RaoEnum;
import ru.bmstu.rk9.rao.rao.RelevantResource;
import ru.bmstu.rk9.rao.rao.ResourceCreateStatement;
import ru.bmstu.rk9.rao.rao.ResourceType;
import ru.bmstu.rk9.rao.generator.RaoExpressionCompiler;

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

	public LocalContext populateWithEnums(RaoEnum enm) {
		for (String id : enm.getType().getValues())
			this.addRawEntry(enm.getType().getName() + "." + id,
					RaoExpressionCompiler.compileType(enm),
					RaoExpressionCompiler.compileType(enm) + "." + id);

		return this;
	}

	public LocalContext populateFromFunction(FunctionAlgorithmic function) {
		if (function.getParameters() != null)
			for (FunctionParameter parameter : function.getParameters())
				if (RaoExpressionCompiler.compileType(parameter.getType())
						.endsWith("_enum"))
					this.addRawEntry(parameter.getName(), RaoExpressionCompiler
							.compileType(parameter.getType()), parameter
							.getName());
				else
					this.addRawEntry(parameter.getName(), RaoExpressionCompiler
							.compileTypePrimitive(parameter.getType()),
							parameter.getName());

		if (RaoExpressionCompiler.compileType(
				((Function) function.eContainer()).getReturnType()).endsWith(
				"_enum"))
			this.populateWithEnums((RaoEnum) (RaoExpressionCompiler
					.resolveAllTypes(((Function) function.eContainer())
							.getReturnType())));

		return this;
	}

	public LocalContext populateFromFunction(FunctionList function) {
		if (function.getParameters() != null)
			for (FunctionParameter parameter : function.getParameters())
				if (RaoExpressionCompiler.compileType(parameter.getType())
						.endsWith("_enum"))
					this.addRawEntry(parameter.getName(), RaoExpressionCompiler
							.compileType(parameter.getType()), parameter
							.getName());
				else
					this.addRawEntry(parameter.getName(), RaoExpressionCompiler
							.compileTypePrimitive(parameter.getType()),
							parameter.getName());

		if (RaoExpressionCompiler.compileType(
				((Function) function.eContainer()).getReturnType()).endsWith(
				"_enum"))
			this.populateWithEnums((RaoEnum) (RaoExpressionCompiler
					.resolveAllTypes(((Function) function.eContainer())
							.getReturnType())));

		return this;
	}

	public LocalContext populateFromGroupBy(GroupBy groupBy) {
		return this.populateWithResourceRename(groupBy.getType(), "current");
	}

	public LocalContext populateWithResourceRename(ResourceType resourceType,
			String newName) {
		for (Parameter parameter : resourceType.getParameters()) {
			this.addRawEntry(
					resourceType.getName() + "." + parameter.getName(),
					RaoExpressionCompiler.compileType(parameter), newName
							+ ".get_" + parameter.getName() + "()");
			this.addRawEntry(parameter.getName(),
					RaoExpressionCompiler.compileType(parameter), newName
							+ ".get_" + parameter.getName() + "()");
		}
		return this;
	}

	public LocalContext populateFromEvent(Event event) {
		for (Parameter parameter : event.getParameters())
			this.addRawEntry(parameter.getName(),
					RaoExpressionCompiler.compileType(parameter), "parameters."
							+ parameter.getName());

		return this;
	}

	public LocalContext populateFromPattern(Pattern pattern) {
		for (Parameter parameter : pattern.getParameters())
			this.addRawEntry(parameter.getName(),
					RaoExpressionCompiler.compileType(parameter), "parameters."
							+ parameter.getName());

		for (RelevantResource relevantResource : pattern.getRelevantResources()) {
			ResourceType type;
			if (relevantResource.getType() instanceof ResourceType)
				type = ((ResourceType) relevantResource.getType());
			else
				type = ((ResourceCreateStatement) relevantResource.getType())
						.getType();

			for (Parameter parameter : type.getParameters())
				this.addRawEntry(
						relevantResource.getName() + "." + parameter.getName(),
						RaoExpressionCompiler.compileType(parameter),
						"resources." + relevantResource.getName() + ".get_"
								+ parameter.getName() + "()");
		}

		return this;
	}

	public void addCreatedResource(
			ResourceCreateStatement resourceCreateStatement) {
		for (Parameter parameter : resourceCreateStatement.getType()
				.getParameters())
			this.addRawEntry(
					resourceCreateStatement.getName() + "."
							+ parameter.getName(),
					RaoExpressionCompiler.compileType(parameter),
					resourceCreateStatement.getName() + ".get_"
							+ parameter.getName() + "()");
	}
}
