package ru.bmstu.rk9.rdo.generator;

import java.util.LinkedList;

import java.util.HashMap;

import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter;

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration;

import ru.bmstu.rk9.rdo.rdo.Function;
import ru.bmstu.rk9.rdo.rdo.FunctionAlgorithmic;
import ru.bmstu.rk9.rdo.rdo.FunctionList;
import ru.bmstu.rk9.rdo.rdo.FunctionParameter;

import ru.bmstu.rk9.rdo.rdo.PatternParameter;
import ru.bmstu.rk9.rdo.rdo.Event;
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Operation;
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Rule;
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource;

import ru.bmstu.rk9.rdo.rdo.GroupBy;

import ru.bmstu.rk9.rdo.rdo.RDOEnum;
import ru.bmstu.rk9.rdo.rdo.EnumID;

import ru.bmstu.rk9.rdo.rdo.Expression;


public class LocalContext
{
	public static class ContextEntry
	{
		public String type;
		public String generated;

		public ContextEntry(String generated, String type)
		{
			this.type = type;
			this.generated = generated;
		}
	}

	private HashMap<String, ContextEntry> index;

	public LocalContext addRawEntry(String name, String type, String generated)
	{
		index.put(name, new ContextEntry(generated, type));
		return this;
	}

	public ContextEntry findEntry(String entry)
	{
		return index.get(entry);
	}

	public LocalContext()
	{
		index = new HashMap<String, ContextEntry>();
	}

	public LocalContext(LocalContext other)
	{
		if(other != null)
			index = new HashMap<String, ContextEntry>(other.index);
		else
			index = new HashMap<String, ContextEntry>();
	}

	public LocalContext populateWithEnums(RDOEnum enm)
	{
		for(EnumID id : enm.getEnums())
			this.addRawEntry(id.getName(), RDOExpressionCompiler.compileType(enm), RDOExpressionCompiler.compileType(enm) + "." + id.getName());

		return this;
	}

	public LocalContext populateFromFunction(FunctionAlgorithmic fun)
	{
		if(fun.getParameters() != null)
			for(FunctionParameter p : fun.getParameters().getParameters())
				if(RDOExpressionCompiler.compileType(p.getType()).endsWith("_enum"))
					this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p.getType()), p.getName());
				else
					this.addRawEntry(p.getName(), RDOExpressionCompiler.compileTypePrimitive(p.getType()), p.getName());

		if(RDOExpressionCompiler.compileType(((Function)fun.eContainer()).getReturntype()).endsWith("_enum"))
			this.populateWithEnums((RDOEnum)(RDOExpressionCompiler.resolveAllSuchAs(((Function)fun.eContainer()).getReturntype())));

		return this;
	}

	public LocalContext populateFromFunction(FunctionList fun)
	{
		if(fun.getParameters() != null)
			for(FunctionParameter p : fun.getParameters().getParameters())
				if(RDOExpressionCompiler.compileType(p.getType()).endsWith("_enum"))
					this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p.getType()), p.getName());
				else
					this.addRawEntry(p.getName(), RDOExpressionCompiler.compileTypePrimitive(p.getType()), p.getName());

		if(RDOExpressionCompiler.compileType(((Function)fun.eContainer()).getReturntype()).endsWith("_enum"))
			this.populateWithEnums((RDOEnum)(RDOExpressionCompiler.resolveAllSuchAs(((Function)fun.eContainer()).getReturntype())));

		return this;
	}

	public LocalContext populateFromGroupBy(GroupBy gb)
	{
		return this.populateWithResourceRename(gb.getType(), "current");
	}

	public LocalContext populateWithResourceRename(ResourceType rtp, String newName)
	{
		for(ResourceTypeParameter p : rtp.getParameters())
		{
			this.addRawEntry(rtp.getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p), newName+ ".get_" + p.getName() + "()");
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p), newName + ".get_" + p.getName() + "()");
		}
		return this;
	}

	public LocalContext populateFromEvent(Event evn)
	{
		for(PatternParameter p : evn.getParameters())
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p), "parameters." + p.getName());

		for(EventRelevantResource relres : evn.getRelevantresources())
		{
			ResourceType type;
			if(relres.getType() instanceof ResourceType)
				type = ((ResourceType)relres.getType());
			else
				type = ((ResourceDeclaration)relres.getType()).getReference();

			for(ResourceTypeParameter p : type.getParameters())
				this.addRawEntry(relres.getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p), "resources." + relres.getName() + ".get_" + p.getName() + "()");
		}

		return this;
	}

	public LocalContext populateFromRule(Rule rule)
	{
		for(PatternParameter p : rule.getParameters())
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p), "parameters." + p.getName());

		for(RuleRelevantResource relres : rule.getRelevantresources())
		{
			ResourceType type;
			if(relres.getType() instanceof ResourceType)
				type = ((ResourceType)relres.getType());
			else
				type = ((ResourceDeclaration)relres.getType()).getReference();

			for(ResourceTypeParameter p : type.getParameters())
				this.addRawEntry(relres.getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p), "resources." + relres.getName() + ".get_" + p.getName() + "()");
		}

		return this;
	}

	public LocalContext populateFromOperation(Operation op)
	{
		for(PatternParameter p : op.getParameters())
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p), "parameters." + p.getName());

		for(OperationRelevantResource relres : op.getRelevantresources())
		{
			ResourceType type;
			if(relres.getType() instanceof ResourceType)
				type = ((ResourceType)relres.getType());
			else
				type = ((ResourceDeclaration)relres.getType()).getReference();

			for(ResourceTypeParameter p : type.getParameters())
				this.addRawEntry(relres.getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p), "resources." + relres.getName() + ".get_" + p.getName() + "()");
		}

		return this;
	}

	public LocalContext tuneForConvert(String relres)
	{
		LinkedList<String> vars = new LinkedList<String>(index.keySet());

		for(String var : vars)
			if(var.startsWith(relres + ".") && var.indexOf(".", relres.length() + 1) == -1)
				index.put(var.replace(relres + ".", ""), index.get(var));

		return this;
	}
}