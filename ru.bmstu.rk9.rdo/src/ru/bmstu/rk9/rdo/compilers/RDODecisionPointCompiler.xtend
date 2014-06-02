package ru.bmstu.rk9.rdo.compilers

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch
import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule

class RDODecisionPointCompiler
{
	def public static compileDecisionPoint(DecisionPoint dpt, String filename)
	{
		val activities = switch dpt
		{
			DecisionPointSome :dpt.activities
			DecisionPointPrior: dpt.activities
		}

		val parameters = activities.map[a |
			if(a.pattern instanceof Operation)
				(a.pattern as Operation).parameters
			else
				(a.pattern as Rule).parameters
		]

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

				private static rdo_lib.DecisionPoint dpt =
					new rdo_lib.DecisionPoint
					(
						"«dpt.fullyQualifiedName»",
						«IF dpt.parent != null»«dpt.parent.fullyQualifiedName».getDPT()«ELSE»null«ENDIF»,
						«IF dpt instanceof DecisionPointPrior»«dpt.priority.compileExpression.value»«ELSE»null«ENDIF»,
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
					«FOR a : activities»
						dpt.addActivity(
							new rdo_lib.DecisionPoint.Activity()
							{
								@Override
								public String getName()
								{
									return "«filename».«a.name»";
								}

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
							}
						);
					«ENDFOR»

					rdo_lib.Simulator.addDecisionPoint(dpt);
				}

				public rdo_lib.DecisionPoint getDPT()
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
							new rdo_lib.DecisionPointSearch.Activity(«IF a.valueafter != null
								»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.after«
								ELSE»rdo_lib.DecisionPointSearch.Activity.ApplyMoment.before«ENDIF»)
							{
								@Override
								public String getName()
								{
									return "«filename».«a.name»";
								}

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

					rdo_lib.Simulator.addDecisionPoint(dpt);
				}
			}
		'''
	}	
}