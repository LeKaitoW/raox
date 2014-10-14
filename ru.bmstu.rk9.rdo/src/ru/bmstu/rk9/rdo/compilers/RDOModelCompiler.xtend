package ru.bmstu.rk9.rdo.compilers

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeKind

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration


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
					«IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
						»ResourceManager<«rtp.fullyQualifiedName»> «r.allContents.head.nameGeneric
							»_«rtp.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»Model current;

			public static void init()
			{
				(new «project»Model()).deploy();
				«FOR r : rs.resources»

					«FOR rtp : r.allContents.filter(typeof(ResourceDeclaration)).toIterable»
						(new «rtp.reference.fullyQualifiedName»(«if (rtp.parameters != null)
							rtp.parameters.compileExpression.value else ""»)).register("«rtp.fullyQualifiedName»");
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
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = new «
							IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
								»ResourceManager<«rtp.fullyQualifiedName»>();
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
		
		
		ret = ret +
			'''
			.put
			(
				"resource_types", new JSONArray()
					«resTypes»
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
}
