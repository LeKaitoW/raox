package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule

import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch
import ru.bmstu.rk9.rdo.rdo.DptSetConditionStatement
import ru.bmstu.rk9.rdo.rdo.DptSetPriorityStatement
import ru.bmstu.rk9.rdo.rdo.Expression
import ru.bmstu.rk9.rdo.rdo.DptSetParentStatement
import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DptSetTerminateConditionStatement
import ru.bmstu.rk9.rdo.rdo.DptEvaluateByStatement
import ru.bmstu.rk9.rdo.rdo.DptCompareTopsStatement

class RDODecisionPointCompiler
{
	def public static compileDecisionPoint(DecisionPointSome dpt, String filename)
	{
		val activities = dpt.activities

		val priorities = activities.map[a | a.priority]

		val parameters = activities.map[a |
			if(a.pattern instanceof Operation)
				(a.pattern as Operation).parameters
			else
				(a.pattern as Rule).parameters
		]

		var setCondStatements = dpt.initStatements.filter(
				s | s instanceof DptSetConditionStatement
			)
		var setPriorStatements = dpt.initStatements.filter(
				s | s instanceof DptSetPriorityStatement
			)
		var setParentStatements = dpt.initStatements.filter(
				s | s instanceof DptSetParentStatement
			)

		var Expression condition
		if (!setCondStatements.empty)
			condition = (setCondStatements.get(0) as DptSetConditionStatement).condition
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

			import ru.bmstu.rk9.rdo.lib.json.*;

			import ru.bmstu.rk9.rdo.lib.*;
			@SuppressWarnings("all")

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
											IF activities.get(i).pattern instanceof Rule»RULE«ELSE»OPERATION_BEGIN«ENDIF»,
										executed
									);

									executed.addResourceEntriesToDatabase(Pattern.ExecutedFrom.PRIOR);

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
							«FOR a : activities»
							.put
							(
								new JSONObject()
									.put("name", "«a.name»")
									.put("pattern", "«a.pattern.fullyQualifiedName»")
							)
							«ENDFOR»
					);
			}
			'''
	}

	def public static compileDecisionPointSearch(DecisionPointSearch dpt, String filename)
	{
		val activities = dpt.activities
		val parameters = activities.map[a | a.pattern.parameters]

		var setCondStatements = dpt.initStatements.filter(
				s | s instanceof DptSetConditionStatement
			)
		var setParentStatements = dpt.initStatements.filter(
				s | s instanceof DptSetParentStatement
			)
		var setTerminateConditionStatements = dpt.initStatements.filter(
				s | s instanceof DptSetTerminateConditionStatement
			)
		var evaluateByStatements = dpt.initStatements.filter(
				s | s instanceof DptEvaluateByStatement
			)
		var compareTopsStatements = dpt.initStatements.filter(
				s | s instanceof DptCompareTopsStatement
			)

		var Expression condition
		if (!setCondStatements.empty)
			condition = (setCondStatements.get(0) as DptSetConditionStatement).condition
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

		var Expression evaluateby
		if (!evaluateByStatements.empty)
			evaluateby = (evaluateByStatements.get(0) as DptEvaluateByStatement).evaluateby
		else
			evaluateby = null

		var boolean comparetops
		if (!compareTopsStatements.empty)
			comparetops = (compareTopsStatements.get(0) as DptCompareTopsStatement).comparetops
		else
			comparetops = false

		return
		'''
			package «filename»;

			import ru.bmstu.rk9.rdo.lib.json.*;

			import ru.bmstu.rk9.rdo.lib.*;
			@SuppressWarnings("all")

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF (activities.get(i).parameters != null && activities.get(i).parameters.size == parameters.get(i).size) ||
					(activities.get(i).parameters == null && activities.get(i).pattern.parameters == null)»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»Model> dpt =
					new DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»Model>
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
								return «evaluateby.compileExpression.value»;
							}
						},
						«IF comparetops»true«ELSE»false«ENDIF»,
						new DecisionPointSearch.DatabaseRetriever<rdo_model.«dpt.eResource.URI.projectName»Model>()
						{
							@Override
							public rdo_model.«dpt.eResource.URI.projectName»Model get()
							{
								return rdo_model.«dpt.eResource.URI.projectName»Model.getCurrent();
							}
						}
					);

				public static void init()
				{
					«FOR a : activities»
						dpt.addActivity(
							new DecisionPointSearch.Activity("«filename».«a.name»",«
								IF a.valueafter != null»DecisionPointSearch.Activity.ApplyMoment.after«
									ELSE»DecisionPointSearch.Activity.ApplyMoment.before«ENDIF»)
							{
								@Override
								public boolean checkActivity()
								{
									return «a.pattern.fullyQualifiedName».findResources(«a.name»);
								}

								@Override
								public Rule executeActivity()
								{
									«a.pattern.fullyQualifiedName» executed = «a.pattern.fullyQualifiedName».executeRule(«a.name»);
									return executed;
								}

								@Override
								public double calculateValue()
								{
									return «IF a.valueafter != null»«a.valueafter.compileExpression.value
										»«ELSE»«a.valuebefore.compileExpression.value»«ENDIF»;
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
					.put("compare_tops", "«IF comparetops»YES«ELSE»NO«ENDIF»")
					.put
					(
						"activities", new JSONArray()
							«FOR a : activities»
							.put
							(
								new JSONObject()
									.put("name", "«a.name»")
									.put("pattern", "«a.pattern.fullyQualifiedName»")
							)
							«ENDFOR»
					);
			}
		'''
	}
}
