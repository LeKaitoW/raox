package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.compilers.Util.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*
import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*

import ru.bmstu.rk9.rao.generator.LocalContext

import ru.bmstu.rk9.rao.rao.ResourceType

import ru.bmstu.rk9.rao.rao.Result

import ru.bmstu.rk9.rao.rao.ResultType

import ru.bmstu.rk9.rao.rao.ResultGetValue
import ru.bmstu.rk9.rao.rao.ResultWatchParameter
import ru.bmstu.rk9.rao.rao.ResultWatchQuant
import ru.bmstu.rk9.rao.rao.ResultWatchState
import ru.bmstu.rk9.rao.rao.ResultWatchValue

import ru.bmstu.rk9.rao.generator.RaoExpression
import ru.bmstu.rk9.rao.rao.ParameterTypeBasic
import ru.bmstu.rk9.rao.rao.RaoEnum

class ResultCompiler
{
	def static compileResult(Result result, String filename)
	{
		val name = result.name
		'''
		package «filename»;

		import java.nio.ByteBuffer;

		import ru.bmstu.rk9.rao.lib.json.*;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.result.*;
		import ru.bmstu.rk9.rao.lib.database.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.notification.*;
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
		switch (type)
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
				val expression = type.expression.compileExpression
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					«type.compileValueType(expression)»
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
							«expression.value»
						)
					);
				}
				'''
			}
			ResultWatchParameter:
			{
				val contextEntry = type.parameter.lookupGlobal
				val expression = new RaoExpression(contextEntry.generated, contextEntry.type);

				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					NotificationManager<Simulator.NotificationCategory> notificationManager = Simulator.getNotificationManager();

					notificationManager.getSubscription(Simulator.NotificationCategory.STATE_CHANGE)
						.addSubscriber(INSTANCE);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_ABORTED)
						.addSubscriber(INSTANCE.finalizer);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_COMPLETE)
						.addSubscriber(INSTANCE.finalizer);

					«type.compileValueType(expression)»
				}

				'''
				+
				expression.compileNumericalStat
			}
			ResultWatchValue:
			{
				val context = (new LocalContext).populateWithResourceRename(type.resource, "this.deleted")
				val expression = type.expression.compileExpressionContext(context)
				expression.value = expression.value.replaceFirst("this.deleted", "(this.deleted = " +
					type.resource.fullyQualifiedName + ".getLastDeleted())")
				'''
				«type.resource.fullyQualifiedName» deleted = null;

				'''
				+
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);
					«type.resource.fullyQualifiedName».getNotificationManager().getSubscription(«
						»ResourceNotificationCategory.RESOURCE_DELETED).addSubscriber(INSTANCE);

					«type.compileValueType(expression)»
				}

				private Statistics.Storeless storelessStats
					= new Statistics.Storeless();

				private «expression.type.toSimpleType» value;

				private «expression.type.toSimpleType» minValue = «expression.type».MAX_VALUE;
				private «expression.type.toSimpleType» maxValue = «expression.type».MIN_VALUE;

				private int watchCounter;

				@Override
				public void fireChange()
				{
					value = «expression.value»;

					if (value < minValue)
						minValue = value;

					if (value > maxValue)
						maxValue = value;

					watchCounter++;

					Simulator.getDatabase().addResultEntry(this);

					storelessStats.next(value);
				}

				@Override
				public ByteBuffer serialize()
				{
					ByteBuffer data = ByteBuffer.allocate(«expression.type.getTypeConstantSize»);
					«expression.type.compileBufferData»

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

					if (storelessStats.initFromDatabase(this))
						data.put("median", storelessStats.getMedian());
				}
				'''
			}
			ResultWatchState:
			{
				val contextEntry = type.logic.compileExpression
				'''
				public static void init()
				{
					Simulator.addResult(INSTANCE);

					NotificationManager notificationManager = Simulator.getNotificationManager();

					notificationManager.getSubscription(Simulator.NotificationCategory.STATE_CHANGE)
						.addSubscriber(INSTANCE);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_ABORTED)
						.addSubscriber(INSTANCE.finalizer);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_COMPLETE)
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
					boolean newValue = «contextEntry.value»;

					double timeNow;

					valueCheck:
					if (newValue != value || watchCounter == 0)
					{
						timeNow = Simulator.getTime();

						if (watchCounter == 0)
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
				val expression = new RaoExpression
				(
					'''«IF type.logic == null»«
							type.resource.fullyQualifiedName».getTemporary().size()«
						ELSE
							»Select.Size(«type.resource.fullyQualifiedName».getTemporary(), logic)«
						ENDIF»''',
					"Integer"
				);

				(if (type.logic != null)
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

					NotificationManager<Simulator.NotificationCategory> notificationManager = Simulator.getNotificationManager();

					notificationManager.getSubscription(Simulator.NotificationCategory.STATE_CHANGE)
						.addSubscriber(INSTANCE);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_ABORTED)
						.addSubscriber(INSTANCE.finalizer);
					notificationManager.getSubscription(Simulator.NotificationCategory.EXECUTION_COMPLETE)
						.addSubscriber(INSTANCE.finalizer);

					INSTANCE.data.put("valueType", "«expression.type.backToRaoType»");
				}

				'''
				+
				expression.compileNumericalStat
			}
		}
	}

	def private static compileNumericalStat(RaoExpression expression)
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

		private «expression.type.toSimpleType» value;

		private «expression.type.toSimpleType» minValue = «expression.type».MAX_VALUE;
		private «expression.type.toSimpleType» maxValue = «expression.type».MIN_VALUE;

		private int watchCounter;

		@Override
		public void fireChange()
		{
			«expression.type.toSimpleType» newValue = «expression.value»;

			if (newValue != value || watchCounter == 0)
				value = newValue;
			else
				return;

			if (value < minValue)
				minValue = value;

			if (value > maxValue)
				maxValue = value;

			watchCounter++;

			Simulator.getDatabase().addResultEntry(this);

			storelessStats.next(Simulator.getTime(), value);
		}

		@Override
		public ByteBuffer serialize()
		{
			ByteBuffer data = ByteBuffer.allocate(«expression.type.getTypeConstantSize»);
			«expression.type.compileBufferData»

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

			if (storelessStats.initFromDatabase(this))
				data.put("median", storelessStats.getMedian());
		}
		'''
	}

	def private static compileBufferData(String type) {
		if (type == "Integer")
			return "data.putInt(value);\n"

		if (type == "Double")
			return "data.putDouble(value);\n"

		if (type == "Boolean")
			return "data.put(value == true ? (byte)1 : (byte)0);\n"

		if (type == "String")
			return "data.put(bytes_of_value);\n"

		if (type.endsWith("_enum"))
			return "data.putShort((short)value.ordinal());\n"

		return ""

	}

	def private static compileValueType(ResultType result, RaoExpression expression) {
		if (expression.type.endsWith("_enum")) '''
			INSTANCE.data
				.put("valueType", "enum")
				.put("enum_origin", "«expression.type.substring(0, expression.type.length - 5)»")
				.put
				(
					"enums", new JSONArray()
						«FOR enumValue : (((result.modelRoot.eAllContents.findFirst[resourceType |
									resourceType instanceof ResourceType && (resourceType as ResourceType).fullyQualifiedName ==
									expression.type.substring(0, expression.type.lastIndexOf('.'))
								] as ResourceType).parameters.findFirst[parameter |
									parameter.name == expression.type.substring(
									expression.type.lastIndexOf('.') + 1, expression.type.length - 5)
								] as ParameterTypeBasic).type as RaoEnum).type.values»
							.put("«enumValue»")
						«ENDFOR»
				);
		''' else '''
			INSTANCE.data.put("valueType", "«expression.type.backToRaoType»");
		'''
	}
}
