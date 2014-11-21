package ru.bmstu.rk9.rdo.compilers

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.Util.*

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeKind

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultGetValue

import ru.bmstu.rk9.rdo.generator.LocalContext


class RDOModelCompiler
{
	def public static compileModel(ResourceSet rs, String project)
	{
		'''
		package rdo_model;

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «project»Model implements ModelState<«project»Model>
		{
			«FOR r : rs.resources»
				«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
					ResourceManager<«rtp.fullyQualifiedName»> «r.allContents.head.nameGeneric
							»_«rtp.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»Model current;

			public static void init()
			{
				(new «project»Model()).deploy();

				Database db = Simulator.getDatabase();
				«FOR r : rs.resources»

					«FOR rtp : r.allContents.filter(typeof(ResourceDeclaration)).toIterable»
						«rtp.reference.fullyQualifiedName» «rtp.name» = new «rtp.reference.fullyQualifiedName»(«if (rtp.parameters != null)
							rtp.parameters.compileExpression.value else ""»);
						«rtp.name».register("«rtp.fullyQualifiedName»");
						db.addResourceEntry(Database.ResourceEntryType.CREATED, «rtp.name», "«rtp.fullyQualifiedName»");

					«ENDFOR»
				«ENDFOR»
			}

			public static «project»Model getCurrent()
			{
				return current;
			}

			private «project»Model()
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name
							»_manager = new ResourceManager<«rtp.fullyQualifiedName»>();
					«ENDFOR»
				«ENDFOR»
			}

			private «project»Model(«project»Model other)
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = other.«r.allContents.head.nameGeneric»_«rtp.name»_manager.copy();
					«ENDFOR»
				«ENDFOR»
			}

			@Override
			public void deploy()
			{
				current = this;

				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						«rtp.fullyQualifiedName».setCurrentManager(«r.allContents.head.nameGeneric»_«rtp.name»_manager);
					«ENDFOR»
				«ENDFOR»
			}

			@Override
			public «project»Model copy()
			{
				return new «project»Model(this);
			}

			@Override
			public boolean checkEqual(«project»Model other)
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						if (!this.«r.allContents.head.nameGeneric»_«rtp.name»_manager.checkEqual(other.«
							r.allContents.head.nameGeneric»_«rtp.name»_manager))
							return false;
					«ENDFOR»
				«ENDFOR»

				return true;
			}

			final static JSONObject modelStructure = new JSONObject()
				«rs.compileModelStructure»;
		}
		'''
	}

	def private static compileModelStructure(ResourceSet rs)
	{
		var ret = ""

		var resTypes = ""
		for(r : rs.resources)
			for(rtp : r.allContents.filter(typeof(ResourceType)).toIterable)
				resTypes = resTypes +
					'''
					.put
					(
						new JSONObject()
							.put("name", "«rtp.fullyQualifiedName»")
							.put("temporary", «rtp.type == ResourceTypeKind.TEMPORARY»)
							.put("structure", «rtp.fullyQualifiedName».structure)
							.put
							(
								"resources", new JSONArray()
									«rtp.compileResourcesInStructure(rs)»
							)
					)
					'''

		var patterns = ""
		for(r : rs.resources)
			for(p : r.allContents.filter(typeof(Pattern)).toIterable)
				patterns = patterns +
					'''
					.put(«p.fullyQualifiedName».structure)
					'''

		var decisionPoints = ""
		for(r : rs.resources)
			for(dpt : r.allContents.filter(typeof(DecisionPoint)).toIterable)
				decisionPoints = decisionPoints +
					'''
					.put(«dpt.fullyQualifiedName».structure)
					'''

		var results = ""
		for(r : rs.resources)
			for(rslt : r.allContents.filter(typeof(ResultDeclaration)).toIterable)		
				results = results +
					'''
					.put
					(
						new JSONObject()
							.put("name", "«rslt.fullyQualifiedName»")
							«rslt.compileResultTypePart»
					)
					'''

		ret = ret +
			'''
			.put
			(
				"resource_types", new JSONArray()
					«resTypes»
			)
			.put
			(
				"patterns", new JSONArray()
					«patterns»
			)
			.put
			(
				"decision_points", new JSONArray()
					«decisionPoints»
			)
			.put
			(
				"results", new JSONArray()
					«results»
			)'''

		return ret
	}

	def private static compileResourcesInStructure(ResourceType rtp, ResourceSet rs)
	{
		var ret = "" 

		for(r : rs.resources)
			for(rss : r.allContents
				.filter(typeof(ResourceDeclaration))
				.filter[s | s.reference == rtp].toIterable)
			{
				ret = ret +
					'''
					.put("«rss.fullyQualifiedName»")
					'''
			}

		return ret
	}

	def private static compileResultTypePart(ResultDeclaration result)
	{
		val type = result.type
		switch(type)
		{
			ResultWatchParameter:
				'''
				.put("type", "watch_par")
				.put("value_type", "«type.parameter.compileExpression.type.backToRDOType»")
				'''
			ResultWatchState:
				'''
				.put("type", "watch_state")
				.put("value_type", "boolean")
				'''
			ResultWatchQuant:
				'''
				.put("type", "watch_quant")
				.put("value_type", "integer")
				'''
			ResultWatchValue:
				'''
				.put("type", "watch_value")
				.put("value_type", "«type.expression.compileExpressionContext(
					(new LocalContext).populateWithResourceRename(
						type.resource, "whatever")).type.backToRDOType»")
				'''
			ResultGetValue:
				'''
				.put("type", "get_value")
				.put("value_type", "«type.expression.compileExpression.type.backToRDOType»")
				'''
		}
	}
}
