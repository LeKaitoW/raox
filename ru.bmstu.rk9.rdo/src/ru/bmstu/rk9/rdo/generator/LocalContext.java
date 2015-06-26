package ru.bmstu.rk9.rdo.generator;

import java.util.HashMap;

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

	HashMap<String, ContextEntry> index;

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
			this.addRawEntry(
					enm.getType().getName() + "." + id,
					RDOExpressionCompiler.compileType(enm),
					RDOExpressionCompiler.compileType(enm) + "." + id);

		return this;
	}

	public LocalContext populateFromFunction(FunctionAlgorithmic fun) {
		if (fun.getParameters() != null)
			for (FunctionParameter p : fun.getParameters())
				if (RDOExpressionCompiler.compileType(p.getType()).endsWith(
						"_enum"))
					this.addRawEntry(p.getName(),
							RDOExpressionCompiler.compileType(p.getType()),
							p.getName());
				else
					this.addRawEntry(p.getName(), RDOExpressionCompiler
							.compileTypePrimitive(p.getType()), p.getName());

		if (RDOExpressionCompiler.compileType(
				((Function) fun.eContainer()).getReturntype())
				.endsWith("_enum"))
			this.populateWithEnums((RDOEnum) (RDOExpressionCompiler
					.resolveAllTypes(((Function) fun.eContainer())
							.getReturntype())));

		return this;
	}

	public LocalContext populateFromFunction(FunctionList fun) {
		if (fun.getParameters() != null)
			for (FunctionParameter p : fun.getParameters())
				if (RDOExpressionCompiler.compileType(p.getType()).endsWith(
						"_enum"))
					this.addRawEntry(p.getName(),
							RDOExpressionCompiler.compileType(p.getType()),
							p.getName());
				else
					this.addRawEntry(p.getName(), RDOExpressionCompiler
							.compileTypePrimitive(p.getType()), p.getName());

		if (RDOExpressionCompiler.compileType(
				((Function) fun.eContainer()).getReturntype())
				.endsWith("_enum"))
			this.populateWithEnums((RDOEnum) (RDOExpressionCompiler
					.resolveAllTypes(((Function) fun.eContainer())
							.getReturntype())));

		return this;
	}

	public LocalContext populateFromGroupBy(GroupBy gb) {
		return this.populateWithResourceRename(gb.getType(), "current");
	}

	public LocalContext populateWithResourceRename(ResourceType rtp,
			String newName) {
		for (ParameterType p : rtp.getParameters()) {
			this.addRawEntry(rtp.getName() + "." + p.getName(),
					RDOExpressionCompiler.compileType(p),
					newName + ".get_" + p.getName() + "()");
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p),
					newName + ".get_" + p.getName() + "()");
		}
		return this;
	}

	public LocalContext populateFromEvent(Event evn) {
		for (ParameterType p : evn.getParameters())
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p),
					"parameters." + p.getName());

		return this;
	}

	public LocalContext populateFromPattern(Pattern pat) {
		for (ParameterType p : pat.getParameters())
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p),
					"parameters." + p.getName());

		for (RelevantResource relres : pat.getRelevantresources()) {
			ResourceType type;
			if (relres.getType() instanceof ResourceType)
				type = ((ResourceType) relres.getType());
			else
				type = ((ResourceCreateStatement) relres.getType()).getType();

			for (ParameterType p : type.getParameters())
				this.addRawEntry(relres.getName() + "." + p.getName(),
						RDOExpressionCompiler.compileType(p), "resources."
								+ relres.getName() + ".get_" + p.getName()
								+ "()");
		}

		return this;
	}

	public void addCreatedResource(ResourceCreateStatement st) {
		for (ParameterType p : st.getType().getParameters())
			this.addRawEntry(st.getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p),
					st.getName() + ".get_" + p.getName() + "()");
	}
}
