package ru.bmstu.rk9.rao.generator

import java.util.HashMap

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.xtext.generator.IFileSystemAccess

import ru.bmstu.rk9.rao.IMultipleResourceGenerator

import ru.bmstu.rk9.rao.compilers.ModelCompiler
import static extension ru.bmstu.rk9.rao.compilers.ConstantCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.SequenceCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.FunctionCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.ResourceTypeCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.PatternCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.EventCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.DecisionPointCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.FrameCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.ResultCompiler.*
import static extension ru.bmstu.rk9.rao.compilers.EnumCompiler.*

import static extension ru.bmstu.rk9.rao.RaoQualifiedNameProvider.*

import static extension ru.bmstu.rk9.rao.generator.RaoNaming.*
import static extension ru.bmstu.rk9.rao.generator.RaoStatementCompiler.*

import ru.bmstu.rk9.rao.rao.RaoModel

import ru.bmstu.rk9.rao.rao.ResourceType

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Function

import ru.bmstu.rk9.rao.rao.Event

import ru.bmstu.rk9.rao.rao.DecisionPoint
import ru.bmstu.rk9.rao.rao.DecisionPointSome
import ru.bmstu.rk9.rao.rao.DecisionPointSearch

import ru.bmstu.rk9.rao.rao.Frame

import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.EnumDeclaration
import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.DefaultMethod
import java.util.Map

class RaoGenerator implements IMultipleResourceGenerator
{
	override void doGenerate(Resource resource, IFileSystemAccess fsa)
	{}

	override void doGenerate(ResourceSet resources, IFileSystemAccess fsa)
	{
		exportVariableInfo(resources)

		val declarationList = new java.util.ArrayList<ResourceCreateStatement>();
		for (resource : resources.resources)
			declarationList.addAll(resource.allContents.filter(typeof(ResourceCreateStatement)).toIterable)

		val simulationList = new java.util.ArrayList<DefaultMethod>();
		for (resource : resources.resources)
			simulationList.addAll(resource.allContents.filter(typeof(DefaultMethod))
				.filter[method | method.name == "init"].toIterable
			)

		val terminateConditionList = new java.util.ArrayList<DefaultMethod>();
		for (resource : resources.resources)
				terminateConditionList.addAll(resource.allContents.filter(
					typeof(DefaultMethod)
				).filter[method | method.name == "terminateCondition"].toIterable)

		var onInit = if (simulationList.size > 0) simulationList.get(0) else null;
		var term = if (terminateConditionList.size > 0) terminateConditionList.get(0) else null;

		for (ecoreResource : resources.resources)
			if (ecoreResource.contents.head != null)
			{
				val filename = (ecoreResource.contents.head as RaoModel).filenameFromURI

				for (enumDeclaration : ecoreResource.allContents.toIterable.filter(typeof(EnumDeclaration)))
					fsa.generateFile(filename + "/" + enumDeclaration.name + ".java", enumDeclaration.compileEnum(filename))

				for (resourceType : ecoreResource.allContents.toIterable.filter(typeof(ResourceType)))
					fsa.generateFile(filename + "/" + resourceType.name + ".java", resourceType.compileResourceType(filename,
						declarationList.filter[r | r.type.fullyQualifiedName == resourceType.fullyQualifiedName]))

				for (constant : ecoreResource.allContents.toIterable.filter(typeof(Constant)))
					fsa.generateFile(filename + "/" + constant.name + ".java", constant.compileConstant(filename))

				for (sequence : ecoreResource.allContents.toIterable.filter(typeof(Sequence)))
					fsa.generateFile(filename + "/" + sequence.name + ".java", sequence.compileSequence(filename))

				for (function : ecoreResource.allContents.toIterable.filter(typeof(Function)))
					fsa.generateFile(filename + "/" + function.type.name + ".java", function.compileFunction(filename))

				for (pattern : ecoreResource.allContents.toIterable.filter(typeof(Pattern)))
					fsa.generateFile(filename + "/" + pattern.name + ".java", pattern.compilePattern(filename))

				for (event : ecoreResource.allContents.toIterable.filter(typeof(Event)))
					fsa.generateFile(filename + "/" + event.name + ".java", event.compileEvent(filename))

				for (decisionPoint : ecoreResource.allContents.toIterable.filter[decisionPoint |
						decisionPoint instanceof DecisionPointSome])
					fsa.generateFile(filename + "/" + (decisionPoint as DecisionPointSome).name + ".java",
						(decisionPoint as DecisionPointSome).compileDecisionPoint(filename))

				for (search : ecoreResource.allContents.toIterable.filter(typeof(DecisionPointSearch)))
					fsa.generateFile(filename + "/" + search.name + ".java",	search.compileDecisionPointSearch(filename))

				for (frame : ecoreResource.allContents.toIterable.filter(typeof(Frame)))
					fsa.generateFile(filename + "/" + frame.name + ".java",	frame.compileFrame(filename))

				for (result : ecoreResource.allContents.toIterable.filter(typeof(Result)))
					fsa.generateFile(filename + "/" + result.name + ".java", result.compileResult(filename))
			}

		fsa.generateFile("rao_model/" + RaoNaming.getProjectName(resources.resources.get(0).URI) +"Model.java",
			ModelCompiler.compileModel(resources, RaoNaming.getProjectName(resources.resources.get(0).URI)))
		fsa.generateFile("rao_model/Standalone.java", compileStandalone(resources, onInit, term))
		fsa.generateFile("rao_model/Embedded.java", compileEmbedded(resources, onInit, term))
	}

	public static final Map<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	def exportVariableInfo(ResourceSet resourceSet)
	{
		variableIndex.clear
		for (ecoreResource : resourceSet.resources)
			variableIndex.put(ecoreResource.resourceName, new GlobalContext)

		for (ecoreResource : resourceSet.resources)
		{
			val globalContext = variableIndex.get(ecoreResource.resourceName)

			for (resource : ecoreResource.allContents.filter(typeof(ResourceCreateStatement))
					.filter(resource | resource.eContainer instanceof RaoModel).toIterable
			)
				globalContext.resources.put(resource.name, globalContext.newResourceReference(resource))

			for (sequence : ecoreResource.allContents.filter(typeof(Sequence)).toIterable)
				globalContext.sequences.put(sequence.name, globalContext.newSequenceReference(sequence))

			for (constant : ecoreResource.allContents.filter(typeof(Constant)).toIterable)
				globalContext.constants.put(constant.name, globalContext.newConstantReference(constant))

			for (function : ecoreResource.allContents.filter(typeof(Function)).toIterable)
				globalContext.functions.put(function.type.name, globalContext.newFunctionReference(function))
		}
	}

	def private compileStandalone(ResourceSet resourceSet,
			DefaultMethod initializeMethod, DefaultMethod terminateConditionMethod
	)
	{
		val project = RaoNaming.getProjectName(resourceSet.resources.get(0).URI)
		'''
		package rao_model;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.common.*;
		import ru.bmstu.rk9.rao.lib.animation.*;
		import ru.bmstu.rk9.rao.lib.result.*;
		@SuppressWarnings("all")

		public class Standalone
		{
			public static void main(String[] args)
			{
				long startTime = System.currentTimeMillis();

				Simulator.initSimulation();
				Simulator.initDatabase(«project»Model.modelStructure);
				Simulator.initModelStructureCache();
				Simulator.initTracer();
				Simulator.initTreeBuilder();

				System.out.println(" === Rao-Simulator ===\n");
				System.out.println("   Project «RaoNaming.getProjectName(resourceSet.resources.get(0).URI)»");
				System.out.println("      Source files are «resourceSet.resources.map[r | r.contents.head.nameGeneric].toString»\n");

				«project»Model.init();

				«IF initializeMethod != null»
					«initializeMethod.body.compileStatement»
				«ENDIF»

				«IF terminateConditionMethod != null»
				Simulator.addTerminateCondition
				(
					new TerminateCondition()
					{
						@Override
						public boolean check()
						{
							«terminateConditionMethod.body.compileStatement»
						}
					}
				);
				«ENDIF»

				«FOR ecoreResource : resourceSet.resources»
					«FOR decisionPoint : ecoreResource.allContents.filter(typeof(DecisionPoint)).toIterable»
						«decisionPoint.fullyQualifiedName».init();
					«ENDFOR»
				«ENDFOR»

				«FOR ecoreResource : resourceSet.resources»
					«FOR result : ecoreResource.allContents.filter(typeof(Result)).toIterable»
						«result.fullyQualifiedName».init();
					«ENDFOR»
				«ENDFOR»

				System.out.println("   Started model");

				int result = Simulator.run();

				if (result == 1)
					System.out.println("\n   Stopped by terminate condition");

				if (result == 0)
					System.out.println("\n   Stopped (no more events)");

				for (Result r : Simulator.getResults())
				{
					r.calculate();
					System.out.println(r.getData().toString(2));
				}

				System.out.println("\n   Finished model in " + String.valueOf((System.currentTimeMillis() - startTime)/1000.0) + "s");
			}
		}
		'''
	}

	def private compileEmbedded(ResourceSet resourceSet,
			DefaultMethod initializeMethod, DefaultMethod terminateConditionMethod
	)
	{
		val project = RaoNaming.getProjectName(resourceSet.resources.get(0).URI)
		'''
		package rao_model;

		import java.util.List;

		import ru.bmstu.rk9.rao.lib.*;
		import ru.bmstu.rk9.rao.lib.simulator.*;
		import ru.bmstu.rk9.rao.lib.common.*;
		import ru.bmstu.rk9.rao.lib.animation.*;
		import ru.bmstu.rk9.rao.lib.result.*;
		@SuppressWarnings("all")

		public class Embedded
		{
			public static void initSimulation(List<AnimationFrame> frames)
			{
				Simulator.initSimulation();
				Simulator.initDatabase(«project»Model.modelStructure);
				Simulator.initModelStructureCache();
				Simulator.initTracer();
				Simulator.initTreeBuilder();

				«project»Model.init();

				«IF initializeMethod != null»
					«initializeMethod.body.compileStatement»
				«ENDIF»

				«IF terminateConditionMethod != null»
				Simulator.addTerminateCondition
				(
					new TerminateCondition()
					{
						@Override
						public boolean check()
						{
							«terminateConditionMethod.body.compileStatement»
						}
					}
				);
				«ENDIF»

				«FOR ecoreResource : resourceSet.resources»
					«FOR decisionPoint : ecoreResource.allContents.filter(typeof(DecisionPoint)).toIterable»
						«decisionPoint.fullyQualifiedName».init();
					«ENDFOR»
				«ENDFOR»

				«FOR ecoreResource : resourceSet.resources»
					«FOR result : ecoreResource.allContents.filter(typeof(Result)).toIterable»
						«result.fullyQualifiedName».init();
					«ENDFOR»
				«ENDFOR»

				«FOR ecoreResource : resourceSet.resources»
					«FOR frame : ecoreResource.allContents.filter(typeof(Frame)).toIterable»
						frames.add(«frame.fullyQualifiedName».INSTANCE);
					«ENDFOR»
				«ENDFOR»
			}

			public static int runSimulation(List<Result> results)
			{
				int result = Simulator.run();

				results.addAll(Simulator.getResults());

				return result;
			}
		}
		'''
	}
}

