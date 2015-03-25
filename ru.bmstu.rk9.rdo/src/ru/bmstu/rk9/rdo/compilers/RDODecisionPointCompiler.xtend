package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule

import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch


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

		val priority = dpt.priority

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
						«IF dpt.condition != null
						»new DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»
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
					«IF dpt.parent == null»
						Simulator.addDecisionPoint(dpt);
					«ELSE»
						«dpt.parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static DecisionPoint getDPT()
				{
					return dpt;
				}

				public static final JSONObject structure = new JSONObject()
					.put("name", "«dpt.fullyQualifiedName»")
					.put("type", "«IF dpt instanceof DecisionPointSome»some«ELSE»prior«ENDIF»")
					.put("parent", «IF dpt.parent != null»"«dpt.parent.fullyQualifiedName»"«ELSE»(String)null«ENDIF»)
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
						«IF dpt.condition != null
						»new DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»,
						new DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.termination.compileExpression.value»;
							}
						},
						new DecisionPointSearch.EvaluateBy()
						{
							@Override
							public double get()
							{
								return «dpt.evaluateby.compileExpression.value»;
							}
						},
						«IF dpt.comparetops»true«ELSE»false«ENDIF»,
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

					«IF dpt.parent == null»
						Simulator.addDecisionPoint(dpt);
					«ELSE»
						«dpt.parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static DecisionPoint getDPT()
				{
					return dpt;
				}

				public static final JSONObject structure = new JSONObject()
					.put("name", "«dpt.fullyQualifiedName»")
					.put("type", "search")
					.put("parent", «IF dpt.parent != null»"«dpt.parent.fullyQualifiedName»"«ELSE»(String)null«ENDIF»)
					.put("compare_tops", "«IF dpt.comparetops»YES«ELSE»NO«ENDIF»")
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
