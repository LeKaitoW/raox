package ru.bmstu.rk9.rao.compilers

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.Util.*

import ru.bmstu.rk9.rao.rao.ResourceType

import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.DecisionPoint

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement
import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.ResultWatchParameter
import ru.bmstu.rk9.rao.rao.ResultWatchValue
import ru.bmstu.rk9.rao.rao.ResultWatchQuantity
import ru.bmstu.rk9.rao.rao.ResultWatchState
import ru.bmstu.rk9.rao.rao.ResultGetValue

import ru.bmstu.rk9.rao.generator.LocalContext
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.RaoModel

class ModelCompiler
{
	def static compileModel(ResourceSet resourceSet, String project)
	{
		'''
		package rao_model;

		«Util.putImports»

		public class «project»Model implements ModelState<«project»Model>
		{
			«FOR ecoreResource : resourceSet.resources»
				«FOR resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable»
					ResourceManager<«resourceType.fullyQualifiedName»> «ecoreResource.allContents.head.nameGeneric
							»_«resourceType.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»Model current;

			public static void init()
			{
				(new «project»Model()).deploy();

				Database db = Simulator.getDatabase();
				«FOR ecoreResource : resourceSet.resources»
					«FOR resource : ecoreResource.allContents.filter(typeof(ResourceCreateStatement))
							.filter(res | res.eContainer instanceof RaoModel).toIterable»
							«IF resource.name != null»
								«resource.type.fullyQualifiedName» «resource.name» = new «
									resource.type.fullyQualifiedName»(«if (resource.parameters != null)
										resource.parameters.compileExpression.value else ""»);
									«resource.name».register("«resource.fullyQualifiedName»");
								db.memorizeResourceEntry(
										«resource.name», Database.ResourceEntryType.CREATED);
							«ELSE»
								db.memorizeResourceEntry(
										new «resource.type.fullyQualifiedName»(«if (resource.parameters != null)
											resource.parameters.compileExpression.value else ""»).register(),
										Database.ResourceEntryType.CREATED);
							«ENDIF»
					«ENDFOR»
				«ENDFOR»
				db.addMemorizedResourceEntries(null, null);
			}

			public static «project»Model getCurrent()
			{
				return current;
			}

			private «project»Model()
			{
				«FOR ecoreResource : resourceSet.resources»
					«FOR resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable»
						this.«ecoreResource.allContents.head.nameGeneric»_«resourceType.name
							»_manager = new ResourceManager<«resourceType.fullyQualifiedName»>();
					«ENDFOR»
				«ENDFOR»
			}

			private «project»Model(«project»Model other)
			{
				«FOR ecoreResource : resourceSet.resources»
					«FOR resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable»
						this.«ecoreResource.allContents.head.nameGeneric»_«resourceType.name»_manager = other.«ecoreResource.allContents.head.nameGeneric»_«resourceType.name»_manager.copy();
					«ENDFOR»
				«ENDFOR»
			}

			@Override
			public void deploy()
			{
				current = this;

				«FOR ecoreResource : resourceSet.resources»
					«FOR resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable»
						«resourceType.fullyQualifiedName».setCurrentManager(«ecoreResource.allContents.head.nameGeneric»_«resourceType.name»_manager);
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
				«FOR ecoreResource : resourceSet.resources»
					«FOR resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable»
						if (!this.«ecoreResource.allContents.head.nameGeneric»_«resourceType.name»_manager.checkEqual(other.«
							ecoreResource.allContents.head.nameGeneric»_«resourceType.name»_manager))
							return false;
					«ENDFOR»
				«ENDFOR»

				return true;
			}

			final static JSONObject modelStructure = new JSONObject()
				«resourceSet.compileModelStructure»;
		}
		'''
	}

	def private static compileModelStructure(ResourceSet resourceSet) {
		var ret = ""
		ret = ret + '''
			.put("name", "«resourceSet.resources.head.allContents.head.nameGeneric»")
		'''

		var enums = ""
		for (ecoreResource : resourceSet.resources)
			for (enumDeclaration : ecoreResource.allContents.filter(typeof(EnumDeclaration)).toIterable)
				enums = enums + '''
					.put
					(
						new JSONObject()
							.put("name", "«enumDeclaration.fullyQualifiedName»")
							.put("structure", «enumDeclaration.fullyQualifiedName».structure)
					)
				'''

		var resourceTypes = ""
		for (ecoreResource : resourceSet.resources)
			for (resourceType : ecoreResource.allContents.filter(typeof(ResourceType)).toIterable)
				resourceTypes = resourceTypes + '''
					.put
					(
						new JSONObject()
							.put("name", "«resourceType.fullyQualifiedName»")
							.put("temporary", true)
							.put("structure", «resourceType.fullyQualifiedName».structure)
							.put
							(
								"resources", new JSONArray()
									«resourceType.compileResourcesInStructure(resourceSet)»
							)
					)
				'''

		var patterns = ""
		for (ecoreResource : resourceSet.resources)
			for (pattern : ecoreResource.allContents.filter(typeof(Pattern)).toIterable)
				patterns = patterns + '''
					.put(«pattern.fullyQualifiedName».structure)
				'''

		var events = ""
		for (ecoreResource : resourceSet.resources)
			for (event : ecoreResource.allContents.filter(typeof(Event)).toIterable)
				events = events + '''
					.put(«event.fullyQualifiedName».structure)
				'''

		var decisionPoints = ""
		for (ecoreResource : resourceSet.resources)
			for (dpt : ecoreResource.allContents.filter(typeof(DecisionPoint)).toIterable)
				decisionPoints = decisionPoints + '''
					.put(«dpt.fullyQualifiedName».structure)
				'''

		var results = ""
		for (ecoreResource : resourceSet.resources)
			for (result : ecoreResource.allContents.filter(typeof(Result)).toIterable)
				results = results + '''
					.put
					(
						new JSONObject()
							.put("name", "«result.fullyQualifiedName»")
							«result.compileResultTypePart»
					)
				'''

		ret = ret +
			'''
			.put
			(
				"enums", new JSONArray()
					«enums»
			)
			.put
			(
				"resource_types", new JSONArray()
					«resourceTypes»
			)
			.put
			(
				"patterns", new JSONArray()
					«patterns»
			)
			.put
			(
				"events", new JSONArray()
					«events»
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

	def private static compileResourcesInStructure(ResourceType resourceType, ResourceSet resourceSet) {
		var ret = ""

		for (ecoreResource : resourceSet.resources)
			for (globalResource : ecoreResource.allContents.filter(typeof(ResourceCreateStatement)).filter(
				s|s.eContainer instanceof RaoModel).filter[s|s.type == resourceType].filter[s|s.name != null].toIterable) {
				ret = ret + '''
					.put("«globalResource.fullyQualifiedName»")
				'''
			}

		return ret
	}

	def private static compileResultTypePart(Result result)
	{
		val type = result.type
		switch (type)
		{
			ResultWatchParameter:
				'''
				.put("type", "watchParameter")
				.put("value_type", "«type.parameter.compileExpression.type.backToRaoType»")
				'''
			ResultWatchState:
				'''
				.put("type", "watchState")
				.put("value_type", "boolean")
				'''
			ResultWatchQuantity:
				'''
				.put("type", "watchQuantity")
				.put("value_type", "int")
				'''
			ResultWatchValue:
				'''
				.put("type", "watchValue")
				.put("value_type", "«type.expression.compileExpressionContext(
					(new LocalContext).populateWithResourceRename(
						type.resource, "whatever")).type.backToRaoType»")
				'''
			ResultGetValue:
				'''
				.put("type", "getValue")
				.put("value_type", "«type.expression.compileExpression.type.backToRaoType»")
				'''
		}
	}
}
