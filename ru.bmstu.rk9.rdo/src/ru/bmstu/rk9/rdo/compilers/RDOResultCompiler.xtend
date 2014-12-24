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

		import ru.bmstu.rk9.rdo.lib.json.*;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class «name» implements Result, Subscriber
		{
			private static «name» INSTANCE = new «name»();

			@Override
			public String getName()
			{
				return "«result.fullyQualifiedName»";
			}

			private JSONObject data = new JSONObject()
				.put("name", this.getName())
				.put("type", "«result.type.compileTypeEnum»");

			@Override
			public JSONObject getData()
			{
				return data;
			}

			«result.type.compileResultBody»
		}
		'''
	}

	def private static String compileTypeEnum(ResultType type)
	{
		switch(type)
		{
			ResultGetValue:       "get_value"
			ResultWatchParameter: "watch_par"
			ResultWatchQuant:     "watch_quant"
			ResultWatchState:     "watch_state"
			ResultWatchValue:     "watch_value"
		}
	}

	def public static compileResultBody(ResultType type)
	{
		switch type
		{
			ResultGetValue:
			{
				val expr = type.expression.compileExpression
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					INSTANCE.data.put("value_type", "«expr.type.backToRDOType»");
				}

				@Override
				public void fireChange() {}

				@Override
				public ByteBuffer serialize()
				{
					return null;
				}

				@Override
				public void calculate()
				{
					data.put
					(
						"value",
						String.valueOf
						(
							«expr.value»
						)
					);
				}
				'''
			}
			ResultWatchParameter:
			{
				val cexpr = type.parameter.lookupGlobal
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);

					INSTANCE.data.put("value_type", "«cexpr.type.backToRDOType»");
				}

				private Statistics.WeightedStoreless storelessStats
					= new Statistics.WeightedStoreless();

				private «cexpr.type.toSimpleType» value;

				private «cexpr.type.toSimpleType» minValue = «cexpr.type».MAX_VALUE;
				private «cexpr.type.toSimpleType» maxValue = «cexpr.type».MIN_VALUE;

				private int watchCounter;

				@Override
				public void fireChange()
				{
					int newValue = «cexpr.generated»;

					if(newValue != value || watchCounter == 0)
						value = newValue;
					else
						return;

					if(value < minValue)
						minValue = value;

					if(value > maxValue)
						maxValue = value;

					watchCounter++;

					Simulator.getDatabase().addResultEntry(this);

					storelessStats.next(Simulator.getTime(), value);
				}

				@Override
				public ByteBuffer serialize()
				{
					ByteBuffer data = ByteBuffer.allocate(«cexpr.type.getTypeConstantSize»);
					«cexpr.type.compileBufferData»

					return data;
				}

				@Override
				public void calculate()
				{
					double mean = storelessStats.getMean();
					double deviation = storelessStats.getStandartDeviation();

					data
						.put("mean", Double.isFinite(mean) ? mean : "N/A")
						.put("deviation", Double.isFinite(deviation) ? deviation : "N/A")
						.put("last", value)
						.put("min", minValue)
						.put("max", maxValue)
						.put("counter", watchCounter);

					if(storelessStats.initFromDatabase(this))
						data.put("median", storelessStats.getMedian());
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
					«type.resource.fullyQualifiedName»
						.getNotifier()
							.getSubscription("ResourceDeleted")
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
				public ByteBuffer serialize()
				{
					«type.resource.fullyQualifiedName» deleted = «type.resource.fullyQualifiedName».getLastDeleted();

					«cexpr.type» value = «cexpr.value»;

					ByteBuffer data = ByteBuffer.allocate(«cexpr.type.getTypeConstantSize»);
					«cexpr.type.compileBufferData»

					return data;
				}

				@Override
				public void calculate()
				{
					
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
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public ByteBuffer serialize()
				{
					Boolean value = «cexpr.value»;

					ByteBuffer data = ByteBuffer.allocate(1);
					«"Boolean".compileBufferData»

					return data;
				}

				@Override
				public void calculate()
				{
					
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
					Simulator.getNotifier().getSubscription("StateChange").addSubscriber(INSTANCE);
				}

				@Override
				public void fireChange()
				{
					Simulator.getDatabase().addResultEntry(this);
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

				@Override
				public void calculate()
				{
					
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
			return "data.put(bytes_of_value);\n"

		if(type.endsWith("_enum"))
			return "data.putShort((short)value.ordinal());\n"

		return ""

	}
}
