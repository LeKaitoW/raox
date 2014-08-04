package ru.bmstu.rk9.rdo.generator

import java.util.HashMap

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rdo.IMultipleResourceGenerator

import ru.bmstu.rk9.rdo.compilers.RDODatabaseCompiler
import static extension ru.bmstu.rk9.rdo.compilers.RDOConstantCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOSequenceCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOFunctionCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOPatternCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDODecisionPointCompiler.*
import static extension ru.bmstu.rk9.rdo.compilers.RDOResultCompiler.*

import static extension ru.bmstu.rk9.rdo.RDOQualifiedNameProvider.*

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOStatementCompiler.*

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Function

import ru.bmstu.rk9.rdo.rdo.Event
import ru.bmstu.rk9.rdo.rdo.Operation
import ru.bmstu.rk9.rdo.rdo.Rule

import ru.bmstu.rk9.rdo.rdo.DecisionPoint
import ru.bmstu.rk9.rdo.rdo.DecisionPointPrior
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch

import ru.bmstu.rk9.rdo.rdo.Results
import ru.bmstu.rk9.rdo.rdo.ResultDeclaration

import ru.bmstu.rk9.rdo.rdo.SimulationRun


class RDOGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		exportVariableInfo(resources)

		val declarationList = new java.util.ArrayList<ResourceDeclaration>();
		for (resource : resources.resources)
			declarationList.addAll(resource.allContents.filter(typeof(ResourceDeclaration)).toIterable)

		val simulationList = new java.util.ArrayList<SimulationRun>();
		for (resource : resources.resources)
			simulationList.addAll(resource.allContents.filter(typeof(SimulationRun)).toIterable)

		var smr = if (simulationList.size > 0) simulationList.get(0) else null;

		for (resource : resources.resources)
			if (resource.contents.head != null)
			{
				val filename = (resource.contents.head as RDOModel).filenameFromURI

				for (e : resource.allContents.toIterable.filter(typeof(ResourceType)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileResourceType(filename,
						declarationList.filter[r | r.reference.fullyQualifiedName == e.fullyQualifiedName]))

				for (e : resource.allContents.toIterable.filter(typeof(ConstantDeclaration)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileConstant(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Sequence)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileSequence(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Function)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileFunction(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Operation)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileOperation(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Rule)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileRule(filename))

				for (e : resource.allContents.toIterable.filter(typeof(Event)))
					fsa.generateFile(filename + "/" + e.name + ".java", e.compileEvent(filename))

				for (e : resource.allContents.toIterable.filter[d |
						d instanceof DecisionPointSome || d instanceof DecisionPointPrior])
					fsa.generateFile(filename + "/" + (e as DecisionPoint).name + ".java",
						(e as DecisionPoint).compileDecisionPoint(filename))

				for (e : resource.allContents.toIterable.filter(typeof(DecisionPointSearch)))
					fsa.generateFile(filename + "/" + e.name + ".java",	e.compileDecisionPointSearch(filename))

				for (e : resource.allContents.toIterable.filter(typeof(ResultDeclaration)))
					fsa.generateFile(filename + "/" + (
						if ((e.eContainer as Results).name != null)	(e.eContainer as Results).name + "_"
							else "") + e.name + ".java", e.compileResult(filename))
			}

		fsa.generateFile("rdo_model/" + RDONaming.getProjectName(resources.resources.get(0).URI) +"_database.java",
			RDODatabaseCompiler.compileDatabase(resources, RDONaming.getProjectName(resources.resources.get(0).URI)))
		fsa.generateFile("rdo_model/StandaloneModel.java", compileStandalone(resources, smr))
		fsa.generateFile("rdo_model/EmbeddedModel.java", compileEmbedded(resources, smr))
	}

	public static HashMap<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	def exportVariableInfo(ResourceSet rs)
	{
		variableIndex.clear
		for(r : rs.resources)
			variableIndex.put(r.resourceName, new GlobalContext)

		for(r : rs.resources)
		{
			val info = variableIndex.get(r.resourceName)

			for(rss : r.allContents.filter(typeof(ResourceDeclaration)).toIterable)
				info.resources.put(rss.name, info.newRSS(rss))

			for(seq : r.allContents.filter(typeof(Sequence)).toIterable)
				info.sequences.put(seq.name, info.newSEQ(seq))

			for(con : r.allContents.filter(typeof(ConstantDeclaration)).toIterable)
				info.constants.put(con.name, info.newCON(con))

			for(fun : r.allContents.filter(typeof(Function)).toIterable)
				info.functions.put(fun.name, info.newFUN(fun))
		}
	}

	def compileStandalone(ResourceSet rs, SimulationRun smr)
	{
		'''
		package rdo_model;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class StandaloneModel
		{
			public static void main(String[] args)
			{
				long startTime = System.currentTimeMillis();

				Simulator.initSimulation();

				System.out.println(" === RDO-Simulator ===\n");
				System.out.println("   Project «RDONaming.getProjectName(rs.resources.get(0).URI)»");
				System.out.println("      Source files are «rs.resources.map[r | r.contents.head.nameGeneric].toString»\n");

				«RDONaming.getProjectName(rs.resources.get(0).URI)»_database.init();

				«IF smr != null»«FOR c :smr.commands»
					«c.compileStatement»
				«ENDFOR»«ENDIF»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(DecisionPoint)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(ResultDeclaration)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				System.out.println("   Started model");

				int result = Simulator.run();

				if (result == 1)
					System.out.println("\n   Stopped by terminate condition");

				if (result == 0)
					System.out.println("\n   Stopped (no more events)");

				System.out.println("\n   Finished model in " + String.valueOf((System.currentTimeMillis() - startTime)/1000.0) + "s");

				System.out.println("\nResults:");
				System.out.println("-------------------------------------------------------------------------------");
				Simulator.printResults();
				System.out.println("-------------------------------------------------------------------------------");
			}
		}
		'''
	}

	def compileEmbedded(ResourceSet rs, SimulationRun smr)
	{
		'''
		package rdo_model;

		import java.util.List;

		import ru.bmstu.rk9.rdo.lib.*;
		@SuppressWarnings("all")

		public class EmbeddedModel
		{
			public static int runSimulation(List<Result> results)
			{
				Simulator.initSimulation();

				«RDONaming.getProjectName(rs.resources.get(0).URI)»_database.init();

				«IF smr != null»«FOR c :smr.commands»
					«c.compileStatement»
				«ENDFOR»«ENDIF»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(DecisionPoint)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				«FOR r : rs.resources»
				«FOR c : r.allContents.filter(typeof(ResultDeclaration)).toIterable»
					«c.fullyQualifiedName».init();
				«ENDFOR»
				«ENDFOR»

				int result = Simulator.run();

				results.addAll(Simulator.getResults());

				return result;
			}
		}
		'''
	}
}

