package ru.bmstu.rk9.rdo.compilers

import java.util.List

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import static extension ru.bmstu.rk9.rdo.compilers.RDOEnumCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.Util.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.ResourceType
import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterType
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterBasic
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
import ru.bmstu.rk9.rdo.rdo.RDORTPParameterString

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.RDOEnum


class RDOResourceTypeCompiler
{
	def public static compileResourceType(ResourceType rtp, String filename, Iterable<ResourceDeclaration> instances)
	{
		'''
		package «filename»;

		public class «rtp.name» implements rdo_lib.«rtp.type.literal.withFirstUpper»Resource, rdo_lib.ResourceComparison<«rtp.name»>
		{
			private static rdo_lib.«rtp.type.literal.withFirstUpper
				»ResourceManager<«rtp.name»> managerCurrent;

			private String name;

			@Override
			public String getName()
			{
				return name;
			}

			«IF rtp.type.literal == "temporary"»
			private Integer number = null;

			@Override
			public Integer getNumber()
			{
				return number;
			}

			«ENDIF»
			public void register(String name)
			{
				this.name = name;
				managerCurrent.addResource(this);
			}

			«IF rtp.type.literal == "temporary"»
			public void register()
			{
				this.number = managerCurrent.getNextNumber();
				managerCurrent.addResource(this);
			}

			«ENDIF»
			public static «rtp.name» getResource(String name)
			{
				return managerCurrent.getResource(name);
			}

			public static java.util.Collection<«rtp.name»> getAll()
			{
				return managerCurrent.getAll();
			}

			«IF rtp.type.literal == "temporary"»
			public static java.util.Collection<«rtp.name»> getTemporary()
			{
				return managerCurrent.getTemporary();
			}

			public static void eraseResource(«rtp.name» res)
			{
				managerCurrent.eraseResource(res);
			}

			«ENDIF»
			private rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> managerOwner = managerCurrent;

			public static void setCurrentManager(rdo_lib.«rtp.type.literal.withFirstUpper»ResourceManager<«rtp.name»> manager)
			{
				managerCurrent = manager;
			}

			«IF rtp.eAllContents.filter(typeof(RDOEnum)).toList.size > 0»// ENUMS«ENDIF»
			«FOR e : rtp.eAllContents.toIterable.filter(typeof(RDOEnum))»
				public enum «e.getEnumParentName(false)»_enum
				{
					«e.makeEnumBody»
				}

			«ENDFOR»
			«FOR parameter : rtp.parameters»
				private «parameter.type.compileType» «parameter.name»«parameter.type.getDefault»;

				public «parameter.type.compileType» get_«parameter.name»()
				{
					return «parameter.name»;
				}

				public «parameter.type.compileType» set_«parameter.name»(«parameter.type.compileType» «parameter.name»)
				{
					if (managerOwner == managerCurrent)
						this.«parameter.name» = «parameter.name»;
					else
						this.copyForNewOwner().«parameter.name» = «parameter.name»;

					return «parameter.name»;
				}

				public «parameter.type.compileType» set_«parameter.name»_after(«parameter.type.compileType» «parameter.name»)
				{
					«parameter.type.compileTypePrimitive» copy = this.«parameter.name»;

					set_«parameter.name»(«parameter.name»);

					return copy;
				}

			«ENDFOR»
			private «rtp.name» copyForNewOwner()
			{
				«rtp.name» copy = new «rtp.name»(«rtp.parameters.compileResourceTypeParametersCopyCall»);
				if (name != null)
				{
					copy.name = name;
					managerCurrent.addResource(copy);
					return copy;
				}
				«IF rtp.type.literal == "temporary"»
				if (number != null)
				{
					copy.number = number;
					managerCurrent.addResource(copy);
					return copy;
				}
				«ENDIF»
				return null;
			}

			public «rtp.name»(«rtp.parameters.compileResourceTypeParameters»)
			{
				«FOR parameter : rtp.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}

			@Override
			public boolean checkEqual(«rtp.name» other)
			{
				«FOR parameter : rtp.parameters»
					if (this.«parameter.name» != other.«parameter.name»)
						return false;
				«ENDFOR»

				return true;
			}
		}
		'''
	}

	def public static compileResourceTypeParametersCopyCall(List<ResourceTypeParameter> parameters)
	{
		'''«IF parameters.size > 0»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}




	def static String getDefault(RDORTPParameterType parameter)
	{
		switch parameter
		{
			RDORTPParameterBasic:
				return if(parameter.^default != null) " = " + parameter.^default.compileExpression.value else ""

			RDORTPParameterEnum:
				return if(parameter.^default != null) " = " + parameter.type.compileType + "." + parameter.^default.name else ""

			RDORTPParameterSuchAs:
				if(parameter.type.compileType.endsWith("_enum"))
					return if(parameter.^default != null) " = " + parameter.^default.compileExpressionContext((new LocalContext).
						populateWithEnums(parameter.type.resolveAllSuchAs as RDOEnum)).value else ""
				else
					return if(parameter.^default != null) " = " + parameter.^default.compileExpression.value else ""

			RDORTPParameterString:
				return if (parameter.^default != null) ' = "' + parameter.^default + '"' else ""

			default:
				return ""
		}
	}

	def public static compileResourceTypeParameters(List<ResourceTypeParameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).type.compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.type.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}
