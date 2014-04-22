package ru.bmstu.rk9.rdo.generator;

import java.util.HashMap;

import ru.bmstu.rk9.rdo.rdo.ResourceType;
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter;

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration;

import ru.bmstu.rk9.rdo.rdo.PatternParameter;
import ru.bmstu.rk9.rdo.rdo.Event;
import ru.bmstu.rk9.rdo.rdo.EventRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Operation;
import ru.bmstu.rk9.rdo.rdo.OperationRelevantResource;
import ru.bmstu.rk9.rdo.rdo.Rule;
import ru.bmstu.rk9.rdo.rdo.RuleRelevantResource;

import ru.bmstu.rk9.rdo.rdo.GroupBy;


public class LocalContext
{
	public static class ContextEntry
	{
		public String type;
		public String generated;

		public ContextEntry(String type, String generated)
		{
			this.type = type;
			this.generated = generated;
		}
	}

	private HashMap<String, ContextEntry> index;

	public void addRawEntry(String name, String type, String generated)
	{
		index.put(name, new ContextEntry(type, generated));
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

	public LocalContext populateFromGroupBy(GroupBy gb)
	{
		for(ResourceTypeParameter p : gb.getType().getParameters())
		{
			this.addRawEntry(gb.getType().getName() + "." + p.getName(), RDOExpressionCompiler.compileType(p), "current.get_" + p.getName() + "()");
			this.addRawEntry(p.getName(), RDOExpressionCompiler.compileType(p), "current.get_" + p.getName() + "()");
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
}