package ru.bmstu.rk9.rao.compilers

import java.util.List

import org.eclipse.emf.ecore.EObject

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.EnumCompiler.*

import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Parameter

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.RaoInt
import ru.bmstu.rk9.rao.rao.RaoDouble
import ru.bmstu.rk9.rao.rao.RaoBoolean
import ru.bmstu.rk9.rao.rao.RaoString
import ru.bmstu.rk9.rao.rao.RaoArray
import ru.bmstu.rk9.rao.rao.RaoEnum

class ResourceTypeCompiler
{
	private static var chunkstart = 0;
	private static var chunkNumber = 0;

	def static compileResourceType(ResourceType resourceType, String filename, Iterable<ResourceCreateStatement> instances)
	{
		'''
		package «filename»;

		import java.nio.ByteBuffer;

		import java.util.Collection;
		import java.util.List;
		import java.util.LinkedList;
		import java.util.ArrayList;
		import java.util.Arrays;
		import java.util.HashMap;

		import ru.bmstu.rk9.rao.lib.json.*;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.resource.*;
		import ru.bmstu.rk9.rao.lib.notification.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		@SuppressWarnings("all")

		public class «resourceType.name» implements Resource, ResourceComparison<«resourceType.name»>
		{
			private static ResourceManager<«resourceType.name»> managerCurrent;

			private String name = null;

			@Override
			public String getName()
			{
				return name;
			}

			@Override
			public String getTypeName()
			{
				return "«resourceType.fullyQualifiedName»";
			}

			private Integer number = null;

			@Override
			public Integer getNumber()
			{
				return number;
			}

			public «resourceType.name» register(String name)
			{
				this.name = name;
				this.number = managerCurrent.getNextNumber();
				managerCurrent.addResource(this);
				lastCreated = this;
				return this;
			}

			public «resourceType.name» register()
			{
				this.number = managerCurrent.getNextNumber();
				managerCurrent.addResource(this);
				lastCreated = this;
				return this;
			}

			private static «resourceType.name» lastCreated;

			public static «resourceType.name» getLastCreated()
			{
				return lastCreated;
			}

			public static «resourceType.name» getResource(String name)
			{
				return managerCurrent.getResource(name);
			}

			public static «resourceType.name» getResource(int number)
			{
				return managerCurrent.getResource(number);
			}

			public static java.util.Collection<«resourceType.name»> getAll()
			{
				return managerCurrent.getAll();
			}

			public static Collection<«resourceType.name»> getTemporary()
			{
				return managerCurrent.getTemporary();
			}

			public static void eraseResource(«resourceType.name» res)
			{
				managerCurrent.eraseResource(res);
				lastDeleted = res;
				notifier.notifySubscribers(ResourceNotificationCategory.RESOURCE_DELETED);
			}

			private static «resourceType.name» lastDeleted;

			public static «resourceType.name» getLastDeleted()
			{
				return lastDeleted;
			}

			private static Notifier<ResourceNotificationCategory> notifier =
				new Notifier<ResourceNotificationCategory>
				(
					ResourceNotificationCategory.class
				);

			public static Notifier<ResourceNotificationCategory> getNotifier()
			{
				return notifier;
			}

			private ResourceManager<«resourceType.name»> managerOwner = managerCurrent;

			public static void setCurrentManager(ResourceManager<«resourceType.name»> manager)
			{
				managerCurrent = manager;
			}

			«FOR parameter : resourceType.parameters»
				private volatile «parameter.compileType» «parameter.name»«parameter.getDefaultValue»;

				public «parameter.compileType» get_«parameter.name»()
				{
					return «parameter.name»;
				}

				public «parameter.compileType» set_«parameter.name»(«parameter.compileType» «parameter.name»)
				{
					if (managerOwner == managerCurrent)
						this.«parameter.name» = «parameter.name»;
					else
						this.copyForNewOwner().«parameter.name» = «parameter.name»;


					Simulator.getDatabase().memorizeResourceEntry(
							this.copy(),
							Database.ResourceEntryType.ALTERED);

					return this.«parameter.name»;
				}
			«ENDFOR»

			private «resourceType.name» copyForNewOwner()
			{
				«resourceType.name» copy = copy();

				managerCurrent.addResource(copy);

				return copy;
			}

			public «resourceType.name» copy()
			{
				«resourceType.name» copy = new «resourceType.name»(«resourceType.parameters.compileParameterTypesCopyCall»);

				copy.name = name;
				copy.number = number;
				copy.managerOwner = managerOwner;

				return copy;
			}

			public «resourceType.name»(«resourceType.parameters.compileParameterTypes»)
			{
				«FOR parameter : resourceType.parameters»
					if («parameter.name» != null)
						this.«parameter.name» = «parameter.name»;
				«ENDFOR»
			}

			@Override
			public boolean checkEqual(«resourceType.name» other)
			{
				«FOR parameter : resourceType.parameters»
					if (!this.«parameter.name».equals(other.«parameter.name»))
						return false;
				«ENDFOR»

				return true;
			}

			public final static JSONObject structure = new JSONObject()
				«resourceType.compileStructure»;

			@Override
			public ByteBuffer serialize()
			{
				int size = «chunkstart + chunkNumber * basicSizes.INT»;
				«resourceType.parameters.filter
				[ parameter |
					val type = parameter.compileType
					if (type == "String" || type.startsWith("java.util.ArrayList"))
						return true
					else
						return false
				].compileBufferCalculation»

				ByteBuffer entry = ByteBuffer.allocateDirect(size);

				«resourceType.parameters.compileSerialization»

				return entry;
			}
		}
		'''
	}

	def private static compileParameterTypesCopyCall(List<Parameter> parameters)
	{
		'''«IF parameters.size > 0»«
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}

	private static class basicSizes
	{
		static val INT = 4
		static val DOUBLE = 8
		static val BOOL = 1
		static val ENUM = 2
	}

	def private static String compileStructure(ResourceType resourceType)
	{
		val parameters = resourceType.parameters
		var offset = 0
		var chunkIndex = 1;

		var constantSizeParameters = ""
		for (parameter : parameters)
		{
			val type = parameter.compileType
			var compiledType = ""

			val compiledOffset = ".put(\"offset\", " + offset + ")"
			val compiledChunk = ".put(\"index\", " + chunkIndex + ")"
			var depth = 0
			var isChunk = false
			var isEnum = false
			var enums = ""

			if (type == "Integer")
			{
				compiledType = "int"
				offset = offset + basicSizes.INT
			}
			if (type == "Double")
			{
				compiledType = "double"
				offset = offset + basicSizes.DOUBLE
			}
			if (type == "Boolean")
			{
				compiledType = "boolean"
				offset = offset + basicSizes.BOOL
			}
			if (type.endsWith("_enum"))
			{
				compiledType = "enum"
				offset = offset + basicSizes.ENUM
				isEnum = true
				enums = enums +
					'''
					.put("enum_origin", "«type.substring(0, type.length - 5)»")
					'''
			}
			if (type.startsWith("java.util.ArrayList"))
			{
				isChunk = true
				compiledType = parameter.arrayType
				if (compiledType.endsWith("_enum"))
				{
					isEnum = true
					enums = enums +
						'''
						.put("enum_origin", "«compiledType.substring(0, compiledType.length - 5)»")
						'''
					compiledType = "enum"
				}
				depth = parameter.arrayDepth
				chunkIndex = chunkIndex + 1
				compiledType = "array\")\n.put(\"array_type\", \"" + compiledType
			}
			if (type == "String")
			{
				isChunk = true
				compiledType = "String"
				chunkIndex = chunkIndex + 1
			}

			constantSizeParameters = constantSizeParameters + '''
				.put
				(
					new JSONObject()
						.put("name", "«parameter.name»")
						.put("type", "«compiledType»")
						«IF isEnum»
							«enums»
						«ENDIF»
						«IF isChunk»
							«compiledChunk»
							«IF depth > 0».put("depth", «depth»)«ENDIF»
						«ELSE»
							«compiledOffset»
						«ENDIF»
				)
				'''
		}

		chunkstart = offset
		chunkNumber = chunkIndex - 1

		return
			'''
			.put
			(
				"parameters", new JSONArray()
					«constantSizeParameters»
			)
			.put("last_offset", «offset»)'''

	}

	def private static String compileBufferCalculation(Iterable<Parameter> parameters)
	{
		var ret = ""

		for (parameter : parameters)
		{
			var typeName = parameter.compileType
			val depth = parameter.arrayDepth
			ret = ret + "\n"
			for (i : 0 ..< depth - 1)
			{
				typeName = typeName.substring("java.util.ArrayList<".length, typeName.length - 1)
				ret = ret + '''
					«i.TABS»size += «basicSizes.INT» * («IF i == 0»«parameter.name»«ELSE»inner«i - 1»«ENDIF».size() + 1);
					«i.TABS»for («typeName» inner«i» : «IF i == 0»«parameter.name»«ELSE»inner«i - 1»«ENDIF»)
					«i.TABS»{
					'''
			}

			if (depth > 0)
			{
				typeName = typeName.substring("java.util.ArrayList<".length, typeName.length - 1)
				ret = ret + '''
				«(depth - 1).TABS»size += «basicSizes.INT» +
				«IF typeName == "Integer"
					»«basicSizes.INT» * «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();«
				ENDIF»«
				IF typeName == "Double"
					»«basicSizes.DOUBLE» * «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();«
				ENDIF»«
				IF typeName == "Boolean"
					»«basicSizes.BOOL» * «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();«
				ENDIF»«
				IF typeName.endsWith("_enum")
					»«basicSizes.ENUM» * «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();«
				ENDIF»«
				IF typeName == "String"
					»«basicSizes.INT» * «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();
				«(depth - 1).TABS»for (String inner : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
				«(depth - 1).TABS»	size += «basicSizes.INT» + inner.getBytes().length;«
				ENDIF»
				'''
			}
			else
			{
				ret = ret + '''
				«IF typeName == "String"»byte[] bytes_of_«parameter.name» = «parameter.name».getBytes();
				size += «basicSizes.INT» + bytes_of_«parameter.name».length;
				«ENDIF»'''
			}

			for (i : 0 ..< depth - 1)
			{
				for (j : 0 ..< depth - i - 2)
					ret = ret + "\t"
				ret = ret + "}\n"
			}
		}

		return ret
	}

	def private static String compileSerialization(Iterable<Parameter> parameters)
	{
		var ret = ""
		val constantSizeParameters = parameters.filter
			[ parameter |
				val type = parameter.compileType
				if (type == "String" || type.startsWith("java.util.ArrayList"))
					return false
				else
					return true
			]
		val variableSizeParameters =  parameters.filter
			[ parameter |
				val type = parameter.compileType
				if (type == "String" || type.startsWith("java.util.ArrayList"))
					return true
				else
					return false
			]

		for (parameter : constantSizeParameters)
		{
			val type = parameter.compileType

			if (type == "Integer")
				ret = ret + '''
					entry.putInt(«parameter.name»);
					'''
			if (type == "Double")
				ret = ret + '''
					entry.putDouble(«parameter.name»);
					'''
			if (type == "Boolean")
				ret = ret + '''
					entry.put(«parameter.name» ? (byte)1 : (byte)0);
					'''
			if (type.endsWith("_enum"))
				ret = ret + '''
					entry.putShort((short)«parameter.name».ordinal());
					'''
		}

		if (chunkNumber > 0)
			ret = ret + '''
				int chunkstart = entry.position(); // «chunkstart»
				int cposition = chunkstart;
				int cnumber = 0;

				LinkedList<Integer> stack = new LinkedList<Integer>();
				stack.add(entry.position());

				entry.position(chunkstart + «basicSizes.INT * chunkNumber»);
				'''

		var parametersNumber = 0
		for (parameter : variableSizeParameters)
		{
			var typeName = parameter.compileType
			val depth = parameter.arrayDepth
			ret = ret + '''

				entry.putInt(stack.peekLast() + «basicSizes.INT * parametersNumber», entry.position());
				{
				'''
			for (i : 0 ..< depth - 1)
			{
				typeName = typeName.substring("java.util.ArrayList<".length, typeName.length - 1)
				ret = ret + '''
					«IF i > 0»
					«(i+1).TABS»int size«i - 1» = inner«i - 1».size();
					«(i+1).TABS»entry.putInt(size«i - 1»);
					«(i+1).TABS»stack.add(entry.position());
					«(i+1).TABS»entry.position(entry.position() + size«i - 1» * «basicSizes.INT»);
					«ENDIF»
					«(i+1).TABS»int counter«i» = 0;
					«(i+1).TABS»for («typeName» inner«i» : «IF i == 0»«parameter.name»«ELSE»inner«i - 1»«ENDIF»)
					«(i+1).TABS»{
					«(i+1).TABS»	entry.putInt(stack.peekLast() + (counter«i»++) * «basicSizes.INT», entry.position());
					'''
			}
			parametersNumber = parametersNumber + 1

			if (depth > 0)
			{
				typeName = typeName.substring("java.util.ArrayList<".length, typeName.length - 1)
				ret = ret + '''
					«depth.TABS»int size«depth - 1» = «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF».size();
					«depth.TABS»entry.putInt(size«depth - 1»);
					«IF typeName == "Integer"
						»«depth.TABS»for (Integer inner«depth - 1» : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
					«depth.TABS»	entry.putInt(inner«depth - 1»);«
					ENDIF»«
					IF typeName == "Double"
						»«depth.TABS»for (Double inner«depth - 1» : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
					«depth.TABS»	entry.putDouble(inner«depth - 1»);«
					ENDIF»«
					IF typeName == "Boolean"
						»«depth.TABS»for (Boolean inner«depth - 1» : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
					«depth.TABS»	entry.put(inner«depth - 1» ? (byte)1 : (byte)0);«
					ENDIF»«
					IF typeName.endsWith("_enum")
						»«depth.TABS»for («typeName» inner«depth - 1» : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
					«depth.TABS»	entry.putShort((short)inner«depth - 1».ordinal());«
					ENDIF»«
					IF typeName == "String"
						»«depth.TABS»stack.add(entry.position());
					«depth.TABS»entry.position(entry.position() + size«depth - 1» * «basicSizes.INT»);
					«depth.TABS»int counter = 0;
					«depth.TABS»for (String inner : «IF depth > 1»inner«depth - 2»«ELSE»«parameter.name»«ENDIF»)
					«depth.TABS»{
					«depth.TABS»	entry.putInt(stack.peekLast() + (counter++) * «basicSizes.INT», entry.position());
					«depth.TABS»	byte[] bytes_of_inner = inner.getBytes();
					«depth.TABS»	int size«depth» = bytes_of_inner.length;
					«depth.TABS»	entry.putInt(size«depth»);
					«depth.TABS»	entry.put(bytes_of_inner);
					«depth.TABS»}
					«depth.TABS»stack.removeLast();«
					ENDIF»
					'''
			}
			else
			{
				ret = ret + '''
					«IF typeName == "String"»	entry.putInt(bytes_of_«parameter.name».length);
						entry.put(bytes_of_«parameter.name»);
					«ENDIF»'''
			}

			for (i : 0 ..< depth - 1)
			{
				ret = ret + (depth - i - 1).TABS + "}\n" +
					if (i < depth - 2) (depth - i - 1).TABS + "stack.removeLast();\n" else ""
			}

			ret = ret + "}\n"
		}

		return ret
	}

	def private static String TABS(int number)
	{
		return '''«FOR i : 0 ..< number»	«ENDFOR»'''
	}

	def private static int getArrayDepth(Parameter parameter)
	{
		var EObject type = parameter
		var depth = 0;

		while (type instanceof RaoArray)
		{
			type = (type as RaoArray).arrayType
			depth = depth + 1
		}

		return depth
	}

	def private static String getArrayType(Parameter parameter)
	{
		var EObject type = parameter

		while (type instanceof RaoArray)
			type = (type as RaoArray).arrayType

		type.compileTypePrimitive
	}

	def static String getDefaultValue(Parameter parameter)
	{
		switch parameter.type
		{
			RaoInt,
			RaoDouble,
			RaoBoolean: {
				if (parameter.^default == null)
					return ""

				return " = " + parameter.^default.compileExpression.value
			}

			RaoEnum: {
				val value = parameter.^default.compileExpression.value
				val fullTypeName = (parameter.type as RaoEnum).getFullEnumName
				if (!checkValidEnumID((parameter.type as RaoEnum).getFullEnumName, value))
					return ""

				return " = " + compileEnumValue(fullTypeName, value)
			}

			RaoString:
				return if (parameter.^default != null) ' = "' + parameter.^default + '"' else ""

			default:
				return ""
		}
	}

	def private static compileParameterTypes(List<Parameter> parameters)
	{
		'''«IF parameters.size > 0»«parameters.get(0).compileType» «
			parameters.get(0).name»«
			FOR parameter : parameters.subList(1, parameters.size)», «
				parameter.compileType» «
				parameter.name»«
			ENDFOR»«
		ENDIF»'''
	}
}
