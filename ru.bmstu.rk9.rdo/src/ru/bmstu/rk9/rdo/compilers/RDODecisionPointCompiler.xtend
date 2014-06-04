package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule

import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch


class RDODecisionPointCompiler
{
	def public static compileDecisionPoint(DecisionPoint dpt, String filename)
	{
		val activities = switch dpt
		{
			DecisionPointSome : dpt.activities
			DecisionPointPrior: dpt.activities.map[a | a.activity]
		}

		val priorities = if(dpt instanceof DecisionPointPrior)
			(dpt as DecisionPointPrior).activities.map[a | a.priority] else null

		val parameters = activities.map[a |
			if(a.pattern instanceof Operation)
				(a.pattern as Operation).parameters
			else
				(a.pattern as Rule).parameters
		]

		val priority = if(dpt instanceof DecisionPointPrior)
			(dpt as DecisionPointPrior).priority else null

		return
			'''
			package «filename»;

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF activities.get(i).parameters.size == parameters.get(i).size»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static rdo_lib.DecisionPoint«IF dpt instanceof DecisionPointPrior»Prior«ENDIF» dpt =
					new rdo_lib.DecisionPoint«IF dpt instanceof DecisionPointPrior»Prior«ENDIF»
					(
						"«dpt.fullyQualifiedName»",
						«IF priority != null»new rdo_lib.DecisionPoint.Priority()
						{
							@Override
							public void calculate()
							{
								priority = «priority.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»,
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
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
							new rdo_lib.DecisionPoint«IF dpt instanceof DecisionPointPrior»Prior«
								ENDIF».Activity«IF dpt instanceof DecisionPointPrior»
							(
								«ELSE»(«ENDIF»"«filename».«activities.get(i).name»"«IF dpt instanceof DecisionPointPrior»,
								«IF priorities.get(i) != null»new rdo_lib.DecisionPoint.Priority()
								{
									@Override
									public void calculate()
									{
										priority = «priorities.get(i).compileExpression.value»;
									}
								}«ELSE»null«ENDIF»
							«ENDIF»)
							{
								@Override
								public boolean checkActivity()
								{
									return «activities.get(i).pattern.fullyQualifiedName
										».findResources(«activities.get(i).name»);
								}

								@Override
								public void executeActivity()
								{
									«activities.get(i).pattern.fullyQualifiedName
										».executeRule(«activities.get(i).name»);
								}
							}
						);

					«ENDFOR»
					«IF dpt.parent == null»
						rdo_lib.Simulator.addDecisionPoint(dpt);
					«ELSE»
						«dpt.parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static rdo_lib.DecisionPoint getDPT()
				{
					return dpt;
				}
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

			public class «dpt.name»
			{
				«FOR i : 0 ..< activities.size»
				«IF (activities.get(i).parameters != null && activities.get(i).parameters.size == parameters.get(i).size) ||
					(activities.get(i).parameters == null && activities.get(i).pattern.parameters == null)»
				private static «activities.get(i).pattern.fullyQualifiedName».Parameters «activities.get(i).name» =
					new «activities.get(i).pattern.fullyQualifiedName».Parameters(«activities.get(i).compileExpression.value»);
				«ENDIF»
				«ENDFOR»

				private static rdo_lib.DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»_database> dpt =
					new rdo_lib.DecisionPointSearch<rdo_model.«dpt.eResource.URI.projectName»_database>
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.condition != null
						»new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.condition.compileExpression.value»;
							}
						}«ELSE»null«ENDIF»,
						new rdo_lib.DecisionPoint.Condition()
						{
							@Override
							public boolean check()
							{
								return «dpt.termination.compileExpression.value»;
							}
						},
						new rdo_lib.DecisionPointSearch.EvaluateBy()
						{
							@Override
							public double get()
							{
								return «dpt.evaluateby.compileExpression.value»;
							}
						},
						«IF dpt.comparetops»true«ELSE»false«ENDIF»,
						new rdo_lib.DecisionPointSearch.DatabaseRetriever<rdo_model.«dpt.eResource.URI.projectName»_database>()
						{
							@Override
							public rdo_model.«dpt.eResource.URI.projectName»_database get()
							{
								return rdo_model.«dpt.eResource.URI.projectName»_database.getCurrent();
							}
						}
					);

				public static void init()
				{
					«FOR a : activities»
						dpt.addActivity(
							new rdo_lib.DecisionPointSearch.Activity("«filename».«a.name»",«
								IF a.valueafter != null»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.after«
									ELSE»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.before«ENDIF»)
							{
								@Override
								public boolean checkActivity()
								{
									return «a.pattern.fullyQualifiedName».findResources(«a.name»);
								}

								@Override
								public void executeActivity()
								{
									«a.pattern.fullyQualifiedName».executeRule(«a.name»);
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
						rdo_lib.Simulator.addDecisionPoint(dpt);
					«ELSE»
						«dpt.parent.fullyQualifiedName».getDPT().addChild(dpt);
					«ENDIF»
				}

				public static rdo_lib.DecisionPoint getDPT()
				{
					return dpt;
				}
			}
		'''
	}
}
