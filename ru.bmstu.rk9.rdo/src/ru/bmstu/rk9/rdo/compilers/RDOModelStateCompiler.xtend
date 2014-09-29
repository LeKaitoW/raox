package ru.bmstu.rk9.rdo.compilers

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeKind

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration
import ru.bmstu.rk9.rdo.rdo.ResourceTrace


class RDOModelStateCompiler
{
	def public static compileModelState(ResourceSet rs, String project)
	{
		'''
		package rdo_model;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «project»State implements ModelState<«project»State>
		{
			«FOR r : rs.resources»
				«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
					«IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
						»ResourceManager<«rtp.fullyQualifiedName»> «r.allContents.head.nameGeneric
							»_«rtp.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»State current;

			private static TraceInfo traceInfo;

			public static TraceInfo getTraceInfo()
			{
				return traceInfo;
			}

			public static void init()
			{
				(new «project»State()).deploy();

				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceDeclaration)).toIterable»
						(new «rtp.reference.fullyQualifiedName»(«if (rtp.parameters != null)
							rtp.parameters.compileExpression.value else ""»)).register("«rtp.fullyQualifiedName»");
					«ENDFOR»

				«ENDFOR»
				«rs.makeDatabasePart»
				«rs.makeTracerPart»
			}

			public static «project»State getCurrent()
			{
				return current;
			}

			private «project»State()
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = new «
							IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
								»ResourceManager<«rtp.fullyQualifiedName»>();
					«ENDFOR»
				«ENDFOR»
			}

			private «project»State(«project»State other)
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
			public «project»State copy()
			{
				return new «project»State(this);
			}

			@Override
			public boolean checkEqual(«project»State other)
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
		}
		'''
	}

	def private static String makeDatabasePart(ResourceSet rs)
	{
		var ret = ""
		var ret2 = ""

		for(r : rs.resources)
			for(rtp : r.allContents.filter(typeof(ResourceType)).toIterable)
				ret = ret + '''db.registerResourceType("«rtp.fullyQualifiedName»", «rtp.fullyQualifiedName».structure, «
						IF rtp.type == ResourceTypeKind.TEMPORARY»true«ELSE»false«ENDIF»);
					'''

		for(r : rs.resources)
			for(rss : r.allContents.filter(typeof(ResourceDeclaration)).toIterable)
				ret2 = ret2 + '''db.registerResource(«rss.reference.fullyQualifiedName».getResource("«
							rss.fullyQualifiedName»"));
					'''

		if(ret2.length != 0)
			ret = ret + "\n" + ret2

		if(ret.length > 0)
			ret = "Database db = Simulator.getDatabase();\n\n" + ret

		return ret + "\n"
	}

	def private static String makeTracerPart(ResourceSet rs)
	{
		var ret = ""

		for(r : rs.resources)
			for(trc : r.allContents.filter(typeof(ResourceTrace)).toIterable)
				ret = ret + '''
					traceInfo.setTraceState(«trc.trace.reference.fullyQualifiedName».getResource("«trc.trace.fullyQualifiedName
						»"), true);
					'''

		if(ret.length > 0)
			ret = "\n" + ret

		ret = "traceInfo = new TraceInfo();\n" + ret

		return ret
	}
}
