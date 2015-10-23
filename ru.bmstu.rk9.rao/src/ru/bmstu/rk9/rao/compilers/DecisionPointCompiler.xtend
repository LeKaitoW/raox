package ru.bmstu.rk9.rao.compilers

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoExpressionCompiler.*

import ru.bmstu.rk9.rao.rao.DecisionPointSome
import ru.bmstu.rk9.rao.rao.DecisionPointSearch
import ru.bmstu.rk9.rao.rao.DptSetConditionStatement
import ru.bmstu.rk9.rao.rao.DptSetPriorityStatement
import ru.bmstu.rk9.rao.rao.Expression
import ru.bmstu.rk9.rao.rao.DptSetParentStatement
import ru.bmstu.rk9.rao.rao.DecisionPoint
import ru.bmstu.rk9.rao.rao.DptSetTerminateConditionStatement
import ru.bmstu.rk9.rao.rao.DptEvaluateByStatement
import ru.bmstu.rk9.rao.rao.DptCompareTopsStatement
import ru.bmstu.rk9.rao.rao.PatternType

class DecisionPointCompiler
{
	def static compileDecisionPoint(DecisionPointSome dpt, String filename)
	{
		val activities = dpt.activities

		val priorities = activities.map[activity | activity.priority]

		val parameters = activities.map[activity | activity.pattern.parameters]

		var setConditionStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetConditionStatement
			)
		var setPriorStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetPriorityStatement
			)
		var setParentStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetParentStatement
			)

		var Expression condition
		if (!setConditionStatements.empty)
			condition = (setConditionStatements.get(0) as DptSetConditionStatement).condition
		else
			condition = null

		var Expression priority
		if (!setPriorStatements.empty)
			priority = (setPriorStatements.get(0) as DptSetPriorityStatement).priority
		else
			priority = null

		var DecisionPoint parent
		if (!setParentStatements.empty)
			parent = (setParentStatements.get(0) as DptSetParentStatement).parent
		else
			parent = null

		return
			'''
			package «filename»;

			«Util.putImports»

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF activities.get(i).parameters.size == parameters.get(i).size»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static DecisionPointPrior dpt =
					new DecisionPointPrior
					(
						"«dpt.fullyQualifiedName»",
						new DecisionPointPrior.Priority()
						{
							@Override
							public void calculate()
							{
								priority = «IF priority != null»«
									priority.compileExpression.value»«ELSE»0«ENDIF»;
							}
						},
						«IF condition != null»
							new DecisionPoint.Condition()
							{
								@Override
								public boolean check()
								{
									return «condition.compileExpression.value»;
								}
							}
						«ELSE»null«ENDIF»
					);

				public static void init()
				{
					«FOR i : 0 ..< activities.size»
						dpt.addActivity(
							new DecisionPointPrior.Activity
							(
								"«activities.get(i).name»",
								new DecisionPoint.Priority()
								{
									@Override
									public void calculate()
									{
										priority = «IF priorities.get(i) != null»«
											priorities.get(i).compileExpression.value»
										«ELSE»0«ENDIF»;
									}
								})
							{
								@Override
								public boolean checkActivity()
								{
									return «activities.get(i).pattern.fullyQualifiedName
										».findResources(«activities.get(i).name»);
								}

								@Override
								public Rule executeActivity()
								{
									«activities.get(i).pattern.fullyQualifiedName» executed =
										«activities.get(i).pattern.fullyQualifiedName
											».executeRule(«activities.get(i).name»);

									Simulator.getDatabase().addDecisionEntry
									(
										dpt, this, Database.PatternType.«
											IF activities.get(i).pattern.type == PatternType.RULE»RULE«ELSE»OPERATION_BEGIN«ENDIF»,
										executed
									);

									executed.addResourceEntriesToDatabase(null);

									return executed;
								}
							}
						);

					«ENDFOR»

					«IF parent == null»
						Simulator.addDecisionPoint(dpt);
					«ELSE»
						parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static DecisionPoint getDPT()
				{
					return dpt;
				}

				public static final JSONObject structure = new JSONObject()
					.put("name", "«dpt.fullyQualifiedName»")
					.put("type", "«IF dpt instanceof DecisionPointSome»some«ELSE»prior«ENDIF»")
					.put("parent", «IF parent != null»"«parent.fullyQualifiedName»"«ELSE»(String)null«ENDIF»)
					.put
					(
						"activities", new JSONArray()
							«FOR activity : activities»
							.put
							(
								new JSONObject()
									.put("name", "«activity.name»")
									.put("pattern", "«activity.pattern.fullyQualifiedName»")
							)
							«ENDFOR»
					);
			}
			'''
	}

	def static compileDecisionPointSearch(DecisionPointSearch dpt, String filename)
	{
		val activities = dpt.activities
		val parameters = activities.map[activity | activity.pattern.parameters]

		var setConditionStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetConditionStatement
			)
		var setParentStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetParentStatement
			)
		var setTerminateConditionStatements = dpt.initStatements.filter(
				statement | statement instanceof DptSetTerminateConditionStatement
			)
		var evaluateByStatements = dpt.initStatements.filter(
				statement | statement instanceof DptEvaluateByStatement
			)
		var compareTopsStatements = dpt.initStatements.filter(
				statement | statement instanceof DptCompareTopsStatement
			)

		var Expression condition
		if (!setConditionStatements.empty)
			condition = (setConditionStatements.get(0) as DptSetConditionStatement).condition
		else
			condition = null

		var DecisionPoint parent
		if (!setParentStatements.empty)
			parent = (setParentStatements.get(0) as DptSetParentStatement).parent
		else
			parent = null

		var Expression termination
		if (!setTerminateConditionStatements.empty)
			termination = (setTerminateConditionStatements.get(0) as DptSetTerminateConditionStatement).termination
		else
			termination = null

		var Expression evaluateBy
		if (!evaluateByStatements.empty)
			evaluateBy = (evaluateByStatements.get(0) as DptEvaluateByStatement).evaluateBy
		else
			evaluateBy = null

		var boolean compareTops
		if (!compareTopsStatements.empty)
			compareTops = (compareTopsStatements.get(0) as DptCompareTopsStatement).compareTops
		else
			compareTops = false

		return
		'''
			package «filename»;

			«Util.putImports»

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF (activities.get(i).parameters != null && activities.get(i).parameters.size == parameters.get(i).size) ||
					(activities.get(i).parameters == null && activities.get(i).pattern.parameters == null)»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static DecisionPointSearch<rao_model.«dpt.eResource.URI.projectName»Model> dpt =
					new DecisionPointSearch<rao_model.«dpt.eResource.URI.projectName»Model>
					(
						"«dpt.fullyQualifiedName»",
						«IF condition != null
						»new DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»,
						new DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «termination.compileExpression.value»;
							}
						},
						new DecisionPointSearch.EvaluateBy()
						{
							@Override
							public double get()
							{
								return «evaluateBy.compileExpression.value»;
							}
						},
						«IF compareTops»true«ELSE»false«ENDIF»,
						new DecisionPointSearch.DatabaseRetriever<rao_model.«dpt.eResource.URI.projectName»Model>()
						{
							@Override
							public rao_model.«dpt.eResource.URI.projectName»Model get()
							{
								return rao_model.«dpt.eResource.URI.projectName»Model.getCurrent();
							}
						}
					);

				public static void init()
				{
					«FOR activity : activities»
						dpt.addActivity(
							new DecisionPointSearch.Activity("«filename».«activity.name»",«
								IF activity.valueAfter != null»DecisionPointSearch.Activity.ApplyMoment.after«
									ELSE»DecisionPointSearch.Activity.ApplyMoment.before«ENDIF»)
							{
								@Override
								public boolean checkActivity()
								{
									return «activity.pattern.fullyQualifiedName».findResources(«activity.name»);
								}

								@Override
								public Rule executeActivity()
								{
									«activity.pattern.fullyQualifiedName» executed = «activity.pattern.fullyQualifiedName».executeRule(«activity.name»);
									return executed;
								}

								@Override
								public double calculateValue()
								{
									return «IF activity.valueAfter != null»«activity.valueAfter.compileExpression.value
										»«ELSE»«activity.valueBefore.compileExpression.value»«ENDIF»;
								}
							}
						);
					«ENDFOR»

					«IF parent == null»
						Simulator.addDecisionPoint(dpt);
					«ELSE»
						«parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static DecisionPoint getDPT()
				{
					return dpt;
				}

				public static final JSONObject structure = new JSONObject()
					.put("name", "«dpt.fullyQualifiedName»")
					.put("type", "search")
					.put("parent", «IF parent != null»"«parent.fullyQualifiedName»"«ELSE»(String)null«ENDIF»)
					.put("compare_tops", "«IF compareTops»YES«ELSE»NO«ENDIF»")
					.put
					(
						"activities", new JSONArray()
							«FOR activity : activities»
							.put
							(
								new JSONObject()
									.put("name", "«activity.name»")
									.put("pattern", "«activity.pattern.fullyQualifiedName»")
							)
							«ENDFOR»
					);
			}
		'''
	}
}
