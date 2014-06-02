package ru.bmstu.rk9.rdo.compilers

import org.eclipse.emf.ecore.resource.ResourceSet

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration


class RDODatabaseCompiler
{
	def public static compileDatabase(ResourceSet rs, String project)
	{
		'''
		package rdo_model;

		public class «project»_database implements rdo_lib.Database<«project»_database>
		{
			«FOR r : rs.resources»
				«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
					rdo_lib.«IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
						»ResourceManager<«rtp.fullyQualifiedName»> «r.allContents.head.nameGeneric
							»_«rtp.name»_manager;
				«ENDFOR»
			«ENDFOR»

			private static «project»_database current;

			public static void init()
			{
				(new «project»_database()).deploy();

				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceDeclaration)).toIterable»
						(new «rtp.reference.fullyQualifiedName»(«if (rtp.parameters != null)
							rtp.parameters.compileExpression.value else ""»)).register("«rtp.name»");
					«ENDFOR»
				«ENDFOR»
			}

			public static «project»_database getCurrent()
			{
				return current;
			}

			private «project»_database()
			{
				«FOR r : rs.resources»
					«FOR rtp : r.allContents.filter(typeof(ResourceType)).toIterable»
						this.«r.allContents.head.nameGeneric»_«rtp.name»_manager = new rdo_lib.«
							IF rtp.type.literal == 'temporary'»Temporary«ELSE»Permanent«ENDIF
								»ResourceManager<«rtp.fullyQualifiedName»>();
					«ENDFOR»
				«ENDFOR»
			}

			private «project»_database(«project»_database other)
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
			public «project»_database copy()
			{
				return new «project»_database(this);
			}

			@Override
			public boolean checkEqual(«project»_database other)
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
}