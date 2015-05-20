package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.compilers.Util.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*
import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*

import ru.bmstu.rk9.rdo.generator.LocalContext

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.Result

import ru.bmstu.rk9.rdo.rdo.ResultType

import ru.bmstu.rk9.rdo.rdo.ResultGetValue
import ru.bmstu.rk9.rdo.rdo.ResultWatchParameter
import ru.bmstu.rk9.rdo.rdo.ResultWatchQuant
import ru.bmstu.rk9.rdo.rdo.ResultWatchState
import ru.bmstu.rk9.rdo.rdo.ResultWatchValue

import ru.bmstu.rk9.rdo.generator.RDOExpression
import ru.bmstu.rk9.rdo.rdo.ParameterTypeBasic
import ru.bmstu.rk9.rdo.rdo.RDOEnum

class RDOResultCompiler
{
	def static compileResult(Result result, String filename)
	{
		val name = result.name
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
			ResultGetValue:       "getValue"
			ResultWatchParameter: "watchPar"
			ResultWatchQuant:     "watchQuant"
			ResultWatchState:     "watchState"
			ResultWatchValue:     "watchValue"
		}
	}

	def private static compileResultBody(ResultType type)
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

					«type.compileValueType(expr)»
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
				val expr = new RDOExpression(cexpr.generated, cexpr.type);

				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					Notifier notifier = Simulator.getNotifier();

					notifier.getSubscription("StateChange")
						.addSubscriber(INSTANCE);
					notifier.getSubscription("ExecutionAborted")
						.addSubscriber(INSTANCE.finalizer);
					notifier.getSubscription("ExecutionComplete")
						.addSubscriber(INSTANCE.finalizer);

					«type.compileValueType(expr)»
				}

				'''
				+
				expr.compileNumericalStat
			}
			ResultWatchValue:
			{
				val context = (new LocalContext).populateWithResourceRename(type.resource, "this.deleted")
				val expr = type.expression.compileExpressionContext(context)
				expr.value = expr.value.replaceFirst("this.deleted", "(this.deleted = " +
					type.resource.fullyQualifiedName + ".getLastDeleted())")
				'''
				«type.resource.fullyQualifiedName» deleted = null;

				'''
				+
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					«type.resource.fullyQualifiedName».getNotifier().getSubscription(«""
						»"ResourceDeleted").addSubscriber(INSTANCE);

					«type.compileValueType(expr)»
				}

				private Statistics.Storeless storelessStats
					= new Statistics.Storeless();

				private «expr.type.toSimpleType» value;

				private «expr.type.toSimpleType» minValue = «expr.type».MAX_VALUE;
				private «expr.type.toSimpleType» maxValue = «expr.type».MIN_VALUE;

				private int watchCounter;

				@Override
				public void fireChange()
				{
					value = «expr.value»;

					if(value < minValue)
						minValue = value;

					if(value > maxValue)
						maxValue = value;

					watchCounter++;

					Simulator.getDatabase().addResultEntry(this);

					storelessStats.next(value);
				}

				@Override
				public ByteBuffer serialize()
				{
					ByteBuffer data = ByteBuffer.allocate(«expr.type.getTypeConstantSize»);
					«expr.type.compileBufferData»

					return data;
				}

				@Override
				public void calculate()
				{
					double mean = storelessStats.getMean();
					double deviation = storelessStats.getStandartDeviation();
					double varcoef = storelessStats.getCoefficientOfVariation();

					data
						.put("mean", Double.isFinite(mean) ? mean : "N/A")
						.put("deviation", Double.isFinite(deviation) ? deviation : "N/A")
						.put("varcoef", Double.isFinite(varcoef) ? varcoef : "N/A")
						.put("last", value)
						.put("min", minValue)
						.put("max", maxValue)
						.put("counter", watchCounter);

					if(storelessStats.initFromDatabase(this))
						data.put("median", storelessStats.getMedian());
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

					Notifier notifier = Simulator.getNotifier();

					notifier.getSubscription("StateChange")
						.addSubscriber(INSTANCE);
					notifier.getSubscription("ExecutionAborted")
						.addSubscriber(INSTANCE.finalizer);
					notifier.getSubscription("ExecutionComplete")
						.addSubscriber(INSTANCE.finalizer);

					INSTANCE.data.put("valueType", "bool");
				}

				private Statistics.LogicStoreless storelessStats
					= new Statistics.LogicStoreless();

				private Subscriber finalizer = new Subscriber()
				{
					@Override
					public void fireChange()
					{
						storelessStats.addState
						(
							value,
							Simulator.getTime() - lastTime
						);
					}
				};

				private double lastTime = 0;

				private int watchCounter;

				boolean value = false;

				@Override
				public void fireChange()
				{
					boolean newValue = «cexpr.value»;

					double timeNow;

					valueCheck:
					if(newValue != value || watchCounter == 0)
					{
						timeNow = Simulator.getTime();

						if(watchCounter == 0)
							break valueCheck;

						double delta = timeNow - lastTime;

						storelessStats.addState(value, delta);
					}
					else
						return;

					lastTime = timeNow;
					value = newValue;

					watchCounter++;

					Simulator.getDatabase().addResultEntry(this);
				}

				@Override
				public ByteBuffer serialize()
				{
					ByteBuffer data = ByteBuffer.allocate(1);
					«"Boolean".compileBufferData»

					return data;
				}

				@Override
				public void calculate()
				{
					data
						.put("last", value)
						.put("counter", watchCounter)
						.put("minTrue", storelessStats.getMinTrue())
						.put("maxTrue", storelessStats.getMaxTrue())
						.put("minFalse", storelessStats.getMinFalse())
						.put("maxFalse", storelessStats.getMaxFalse())
						.put("percent", storelessStats.getPercent());
				}
				'''
			}
			ResultWatchQuant:
			{
				val context = (new LocalContext).populateWithResourceRename(type.resource, "current")
				val expr = new RDOExpression
				(
					'''«IF type.logic == null»«
							type.resource.fullyQualifiedName».getTemporary().size()«
						ELSE
							»Select.Size(«type.resource.fullyQualifiedName».getTemporary(), logic)«
						ENDIF»''',
					"Integer"
				);

				(if(type.logic != null)
					'''
					private static Select.Checker logic =
						new Select.Checker<«type.resource.fullyQualifiedName»>()
						{
							@Override
							public boolean check(«type.resource.fullyQualifiedName» current)
							{
								return «type.logic.compileExpressionContext(context).value»;
							}
						};

					'''
				else
					"")
				+
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					Notifier notifier = Simulator.getNotifier();

					notifier.getSubscription("StateChange")
						.addSubscriber(INSTANCE);
					notifier.getSubscription("ExecutionAborted")
						.addSubscriber(INSTANCE.finalizer);
					notifier.getSubscription("ExecutionComplete")
						.addSubscriber(INSTANCE.finalizer);

					INSTANCE.data.put("valueType", "«expr.type.backToRDOType»");
				}

				'''
				+
				expr.compileNumericalStat
			}
		}
	}

	def private static compileNumericalStat(RDOExpression expr)
	{
		'''
		private Statistics.WeightedStoreless storelessStats
			= new Statistics.WeightedStoreless();

		private Subscriber finalizer = new Subscriber()
		{
			@Override
			public void fireChange()
			{
				storelessStats.next
				(
					Simulator.getTime(),
					Double.NaN
				);
			}
		};

		private «expr.type.toSimpleType» value;

		private «expr.type.toSimpleType» minValue = «expr.type».MAX_VALUE;
		private «expr.type.toSimpleType» maxValue = «expr.type».MIN_VALUE;

		private int watchCounter;

		@Override
		public void fireChange()
		{
			«expr.type.toSimpleType» newValue = «expr.value»;

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
			ByteBuffer data = ByteBuffer.allocate(«expr.type.getTypeConstantSize»);
			«expr.type.compileBufferData»

			return data;
		}

		@Override
		public void calculate()
		{
			double mean = storelessStats.getMean();
			double deviation = storelessStats.getStandartDeviation();
			double varcoef = storelessStats.getCoefficientOfVariation();

			data
				.put("mean", Double.isFinite(mean) ? mean : "N/A")
				.put("deviation", Double.isFinite(deviation) ? deviation : "N/A")
				.put("varcoef", Double.isFinite(varcoef) ? varcoef : "N/A")
				.put("last", value)
				.put("min", minValue)
				.put("max", maxValue)
				.put("counter", watchCounter);

			if(storelessStats.initFromDatabase(this))
				data.put("median", storelessStats.getMedian());
		}
		'''
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

	def private static compileValueType(ResultType result, RDOExpression expr)
	{
		if(expr.type.endsWith("_enum"))
			'''
			INSTANCE.data
				.put("valueType", "enum")
				.put("enum_origin", "«expr.type.substring(0, expr.type.length - 5)»")
				.put
				(
					"enums", new JSONArray()
						«FOR e : (((result.modelRoot.eAllContents.findFirst
							[r | r instanceof ResourceType && (r as ResourceType).fullyQualifiedName
								== expr.type.substring(0, expr.type.lastIndexOf('.'))] as ResourceType)
									.parameters.findFirst[p | p.name == expr.type.substring(
										expr.type.lastIndexOf('.') + 1, expr.type.length - 5)]
											 as ParameterTypeBasic).type as RDOEnum).type.values»
							.put("«e.name»")
						«ENDFOR»
				);
			'''
		else
		'''
		INSTANCE.data.put("valueType", "«expr.type.backToRDOType»");
		'''
	}
}
