package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.compilers.Util.*

import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.ResultType

import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue

class RDOResultCompiler
{
	def public static compileResult(ResultDeclaration result, String filename)
	{
		val name =
			(if ((result.eContainer as Results).name != null)
				(result.eContainer as Results).name + "_" else "")
			+ result.name
		'''
		package «filename»;

		import java.nio.ByteBuffer;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «name» implements Result, Subscriber
		{
			@Override
			public String getName()
			{
				return "«result.fullyQualifiedName»";
			}

			@Override
			public Result.Type getType()
			{
				return «result.type.compileTypeEnum»;
			}

			private static «name» INSTANCE = new «name»();

			«result.type.compileResultBody»
		}
		'''
	}

	def private static String compileTypeEnum(ResultType type)
	{
		switch(type)
		{
			ResultGetValue:       "Result.Type.GET_VALUE"
			ResultWatchParameter: "Result.Type.WATCH_PAR"
			ResultWatchQuant:     "Result.Type.WATCH_QUANT"
			ResultWatchState:     "Result.Type.WATCH_STATE"
			ResultWatchValue:     "Result.Type.WATCH_VALUE"
		}
	}

	def public static compileResultBody(ResultType type)
	{
		switch type
		{
			ResultGetValue:
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getDatabase().addSensitivity("«type.eContainer.fullyQualifiedName»");
				}

				@Override
				public void fireChange() {}

				@Override
				public String get()
				{
					return "«(type.eContainer as ResultDeclaration).name»\t\t|\tType: get_value\t\t|\tValue: " +
						String.valueOf(«type.expression.compileExpression.value»);
				}

				@Override
				public ByteBuffer serialize()
				{
					return null;
				}
				'''
			ResultWatchParameter:
			{
				val cexpr = type.parameter.lookupGlobal
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getDatabase().registerResult(INSTANCE);
					Simulator.getDatabase().addSensitivity("«type.eContainer.fullyQualifiedName»");
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public String get()
				{
					return "";
				}

				@Override
				public ByteBuffer serialize()
				{
					«cexpr.type» value = «cexpr.generated»;

					ByteBuffer data = ByteBuffer.allocate(«cexpr.type.getTypeSize("value")»);
					«cexpr.type.compileBufferData»

					return data;
				}
				'''
			}
			ResultWatchValue:
			{
				val context = (new LocalContext).populateWithResourceRename(type.resource, "deleted")
				val cexpr = type.expression.compileExpressionContext(context)
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getDatabase().registerResult(INSTANCE);
					Simulator.getDatabase().addSensitivity("«type.eContainer.fullyQualifiedName»");
					«type.resource.fullyQualifiedName»
						.getNotifier()
							.getSubscription("RESOURCE.DELETED")
								.addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					«IF type.logic != null»«
						type.resource.fullyQualifiedName» deleted = «type.resource.fullyQualifiedName».getLastDeleted();

					if(«type.logic.compileExpressionContext(context).value»)
						«ENDIF»Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public String get()
				{
					return "";
				}

				@Override
				public ByteBuffer serialize()
				{
					«type.resource.fullyQualifiedName» deleted = «type.resource.fullyQualifiedName».getLastDeleted();

					«cexpr.type» value = «cexpr.value»;

					ByteBuffer data = ByteBuffer.allocate(«cexpr.type.getTypeSize("value")»);
					«cexpr.type.compileBufferData»

					return data;
				}
				'''
			}
			ResultWatchState:
			{
				val cexpr = type.logic.compileExpression
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getDatabase().registerResult(INSTANCE);
					Simulator.getDatabase().addSensitivity("«type.eContainer.fullyQualifiedName»");
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public String get()
				{
					return "";
				}

				@Override
				public ByteBuffer serialize()
				{
					Boolean value = «cexpr.value»;

					ByteBuffer data = ByteBuffer.allocate(1);
					«"Boolean".compileBufferData»

					return data;
				}
				'''

			}
			ResultWatchQuant:
			{
				val context = (new LocalContext).populateWithResourceRename(type.resource, "current")
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getDatabase().registerResult(INSTANCE);
					Simulator.getDatabase().addSensitivity("«type.eContainer.fullyQualifiedName»");
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public String get()
				{
					return "";
				}

				«IF type.logic != null»
				private static Select.Checker logic =
					new Select.Checker<«type.resource.fullyQualifiedName»>()
					{
						@Override
						public boolean check(«type.resource.fullyQualifiedName» current)
						{
							return «type.logic.compileExpressionContext(context).value»;
						}
					};

				«ENDIF»
				@Override
				public ByteBuffer serialize()
				{
					int count = «
						IF type.logic == null»«
							type.resource.fullyQualifiedName».getTemporary().size()«
						ELSE
							»Select.Size(«type.resource.fullyQualifiedName».getTemporary(), logic)«
						ENDIF»;

					ByteBuffer data = ByteBuffer.allocate(4);
					data.putInt(count);

					return data;
				}
				'''
			}
		}
	}

	def private static compileBufferData(String type)
	{
		if(type == "Integer")
			return "data.putInt(value);\n"

		if(type == "Double")
			return "data.putDouble(value);\n"

		if(type == "Boolean")
			return "data.put(value == true ? (byte)1 : (byte)0);\n"

		if(type == "String")
			return "data.put(value.getBytes());\n"

		if(type.endsWith("_enum"))
			return "data.putShort((short)value.ordinal());\n"

		return ""

	}
}
