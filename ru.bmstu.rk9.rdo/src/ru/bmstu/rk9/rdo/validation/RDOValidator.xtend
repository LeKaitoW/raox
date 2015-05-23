package ru.bmstu.rk9.rdo.validation

import java.util.List
import java.util.LinkedList
import java.util.ArrayList

import java.util.Map
import java.util.HashMap

import com.google.inject.Inject

import org.eclipse.xtext.validation.Check

import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider

import org.eclipse.xtext.resource.IContainer

import org.eclipse.emf.ecore.resource.Resource

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import ru.bmstu.rk9.rdo.generator.GlobalContext

import static extension ru.bmstu.rk9.rdo.generator.RDONaming.*
import static extension ru.bmstu.rk9.rdo.generator.RDOExpressionCompiler.*

import ru.bmstu.rk9.rdo.rdo.RdoPackage

import ru.bmstu.rk9.rdo.rdo.RDOModel

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionTable

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.ParameterType
import ru.bmstu.rk9.rdo.rdo.PatternSelectMethod
import ru.bmstu.rk9.rdo.rdo.RelevantResource
import ru.bmstu.rk9.rdo.rdo.Event

import ru.bmstu.rk9.rdo.rdo.DecisionPoint

import ru.bmstu.rk9.rdo.rdo.Frame

import ru.bmstu.rk9.rdo.rdo.Result

import ru.bmstu.rk9.rdo.rdo.RDOInt
import ru.bmstu.rk9.rdo.rdo.RDOEnum
import ru.bmstu.rk9.rdo.rdo.DefaultMethod
import ru.bmstu.rk9.rdo.rdo.DecisionPointSome
import ru.bmstu.rk9.rdo.rdo.DptSetConditionStatement
import ru.bmstu.rk9.rdo.rdo.DptSetPriorityStatement
import ru.bmstu.rk9.rdo.rdo.DptSetParentStatement
import ru.bmstu.rk9.rdo.rdo.DptEvaluateByStatement
import ru.bmstu.rk9.rdo.rdo.DptCompareTopsStatement
import ru.bmstu.rk9.rdo.rdo.DptSetTerminateConditionStatement
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearch
import ru.bmstu.rk9.rdo.rdo.FunctionType
import ru.bmstu.rk9.rdo.rdo.PatternType

class RDOValidator extends AbstractRDOValidator
{

	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider

	@Inject
	private IContainer.Manager containerManager

	private static List<Resource> resourceIndex = new LinkedList<Resource>
	private static HashMap<String, GlobalContext> variableIndex = new HashMap<String, GlobalContext>

	@Check
	def exportResources(RDOModel model)
	{
		val index = resourceDescriptionsProvider.createResourceDescriptions
		val resDesc = index.getResourceDescription(model.eResource.URI)

		resourceIndex.clear

		if(resDesc == null)
			return
		else
		{
			for(IContainer c : containerManager.getVisibleContainers(resDesc, index))
				for(IResourceDescription rd : c.getResourceDescriptions())
				{
					resourceIndex.add(model.eResource.resourceSet.getResource(rd.URI, true))
					if(!variableIndex.containsKey(resourceIndex.last.resourceName))
						variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
				}

			if(variableIndex.size != resourceIndex.size)
			{
				variableIndex.clear
				for(r : resourceIndex)
					variableIndex.put(resourceIndex.last.resourceName, new GlobalContext)
			}
		}
	}

	@Check
	def exportResourceCreateStatements(ResourceCreateStatement rss)
	{
		val model = rss.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val resindex = variableIndex.get(model.nameGeneric).resources
		val resmodel = model.eAllContents.filter(typeof(ResourceCreateStatement)).toMap[name]
		for(r : resmodel.keySet)
			if(resindex.get(r) == null || r == rss.name)
				resindex.put(r, variableIndex.get(model.nameGeneric).newRSS(resmodel.get(r)))

		clearMissing(resindex, resmodel)
	}

	@Check
	def exportSequences(Sequence seq)
	{
		val model = seq.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val seqindex = variableIndex.get(model.nameGeneric).sequences
		val seqmodel = model.eAllContents.filter(typeof(Sequence)).toMap[name]
		for(r : seqmodel.keySet)
			if(seqindex.get(r) == null || r == seq.name)
				seqindex.put(r, variableIndex.get(model.nameGeneric).newSEQ(seqmodel.get(r)))

		clearMissing(seqindex, seqmodel)
	}

	@Check
	def exportConstants(Constant con)
	{
		val model = con.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val conindex = variableIndex.get(model.nameGeneric).constants
		val conmodel = model.eAllContents.filter(typeof(Constant)).toMap[name]
		for(r : conmodel.keySet)
			if(conindex.get(r) == null || r == con.name)
				conindex.put(r, variableIndex.get(model.nameGeneric).newCON(conmodel.get(r)))

		clearMissing(conindex, conmodel)
	}

	@Check
	def exportFunctions(Function fun)
	{
		val model = fun.modelRoot
		if(variableIndex == null || variableIndex.get(model.nameGeneric) == null)
			return

		val funindex = variableIndex.get(model.nameGeneric).functions
		val funmodel = model.eAllContents.filter(typeof(Function)).toMap[type.name]
		for(r : funmodel.keySet)
			if(funindex.get(r) == null || r == fun.type.name)
				funindex.put(r, variableIndex.get(model.nameGeneric).newFUN(funmodel.get(r)))

		clearMissing(funindex, funmodel)
	}

	def private clearMissing(Map<String, ?> index, Map<String, ?> model)
	{
		if(index.size != model.size)
		{
			var iter = index.keySet.iterator
			while(iter.hasNext)
			{
				val next = iter.next
				if(model.get(next) == null)
				{
					index.remove(next)
					iter = index.keySet.iterator
				}
			}
		}
	}

	def private checkDefaultMethodCountGeneric(EObject parent,
			Iterable<DefaultMethod> methods,
			Map<String, DefaultMethodsHelper.MethodInfo> counts
	)
	{
		for(d : methods) {
			if (!counts.containsKey(d.method.name))
				error("Error - incorrect default method name", d,
					d.getNameStructuralFeature
				)
			else if (counts.get(d.method.name).count > 0)
				error("Error - default method cannot be set more than once", d,
					d.getNameStructuralFeature
				)
			else {
				var count = counts.get(d.method.name)
				count.count++
				counts.put(d.method.name, count)
			}
		}

		for(k : counts.keySet)
			if (counts.get(k).count == 0) {
				switch counts.get(k).action {
					case WARNING:
						warning("Warning - default method " + k + " not set",
								parent,
								parent.getNameStructuralFeature)
					case ERROR:
						error("Error - default method " + k + " not set",
								parent,
								parent.getNameStructuralFeature)
					default: {}
				}
			}
	}

	@Check
	def checkDefaultMethodGlobalCount(RDOModel model)
	{
		var Map<String, DefaultMethodsHelper.MethodInfo> counts =
				new HashMap<String, DefaultMethodsHelper.MethodInfo>()
		for (v : DefaultMethodsHelper.GlobalMethodInfo.values)
			counts.put(v.name,
					new DefaultMethodsHelper.MethodInfo(v.validatorAction)
			)

		var methods = model.objects.filter(typeof(DefaultMethod))
		checkDefaultMethodCountGeneric(model, methods, counts)
	}

	@Check
	def checkDefaultMethodPatternCount(Pattern pat)
	{
		var Map<String, DefaultMethodsHelper.MethodInfo> counts =
				new HashMap<String, DefaultMethodsHelper.MethodInfo>()
		switch (pat.type)
		{
			case RULE:
				for (v : DefaultMethodsHelper.RuleMethodInfo.values)
					counts.put(v.name,
							new DefaultMethodsHelper.MethodInfo(v.validatorAction))
			case OPERATION,
			case KEYBOARD:
				for (v : DefaultMethodsHelper.OperationMethodInfo.values)
					counts.put(v.name,
							new DefaultMethodsHelper.MethodInfo(v.validatorAction))
		}

		checkDefaultMethodCountGeneric(pat, pat.defaultMethods, counts)
	}

	@Check
	def checkDuplicateNamesForEntities(RDOModel model)
	{
		val List<String> entities = new ArrayList<String>()
		val List<String> duplicates = new ArrayList<String>()

		val List<EObject> checklist = model.eAllContents.filter[e |
			e instanceof ResourceType        ||
			e instanceof ResourceCreateStatement ||
			e instanceof Sequence            ||
			e instanceof Constant ||
			e instanceof Function            ||
			e instanceof Pattern             ||
			e instanceof DecisionPoint       ||
			e instanceof Frame               ||
			e instanceof Result
		].toList

		for(e : checklist)
		{
			val name = e.fullyQualifiedName
			if(entities.contains(name))
			{
				if(!duplicates.contains(name))
					duplicates.add(name)
			}
			else
				entities.add(name)
		}

		if(!duplicates.empty)
			for(e : checklist)
			{
				val name = e.fullyQualifiedName
				val hasNoName = (name.contains(".")
					&& name.substring(name.lastIndexOf(".") +1 ) == "null"
				)
				if(!hasNoName && duplicates.contains(name))
					error("Error - multiple declarations of object '" + name + "'.", e,
						e.getNameStructuralFeature)
			}
	}

	@Check
	def checkNamesInPatterns (Pattern pat)
	{
		val List<EObject> paramlist  = pat.eAllContents.filter[e |
			e instanceof ParameterType
		].toList
		val List<EObject> relreslist = pat.eAllContents.filter[e |
			e instanceof RelevantResource].toList

		for(e : paramlist)
			if(relreslist.map[r | r.nameGeneric].contains(e.nameGeneric))
				error("Error - parameter name shouldn't match relevant resource name '" + e.nameGeneric + "'.", e,
					e.getNameStructuralFeature)

		for(e : relreslist)
			if(paramlist.map[r | r.nameGeneric].contains(e.nameGeneric))
				error("Error - relevant resource name shouldn't match parameter name '" + e.nameGeneric + "'.", e,
					e.getNameStructuralFeature)
	}

	def private EStructuralFeature getNameStructuralFeature (EObject object)
	{
		switch object
		{
			ResourceType:
				RdoPackage.eINSTANCE.META_RelResType_Name

			ResourceCreateStatement:
				RdoPackage.eINSTANCE.META_RelResType_Name

			Sequence:
				RdoPackage.eINSTANCE.sequence_Name

			FunctionType:
				RdoPackage.eINSTANCE.functionType_Name

			Pattern:
				RdoPackage.eINSTANCE.pattern_Name

			Event:
				RdoPackage.eINSTANCE.event_Name

			DecisionPoint:
				RdoPackage.eINSTANCE.decisionPoint_Name

			Frame:
				RdoPackage.eINSTANCE.frame_Name

			Result:
				RdoPackage.eINSTANCE.result_Name

			ParameterType:
				RdoPackage.eINSTANCE.parameterType_Name

			RelevantResource:
				RdoPackage.eINSTANCE.relevantResource_Name

			DptSetConditionStatement:
				RdoPackage.eINSTANCE.dptSetConditionStatement_Name

			DptSetParentStatement:
				RdoPackage.eINSTANCE.dptSetParentStatement_Name

			DptEvaluateByStatement:
				RdoPackage.eINSTANCE.dptEvaluateByStatement_Name

			DptSetPriorityStatement:
				RdoPackage.eINSTANCE.dptSetPriorityStatement_Name

			DptCompareTopsStatement:
				RdoPackage.eINSTANCE.dptCompareTopsStatement_Name

			DptSetTerminateConditionStatement:
				RdoPackage.eINSTANCE.dptSetTerminateConditionStatement_Name
		}
	}

	@Check
	def checkForCombinationalChioceMethod(Pattern pat)
	{
		var haveselectmethods = false
		var iscombinatorial = false

		for(e : pat.eAllContents.toList.filter(typeof(PatternSelectMethod)))
		{
			switch e.eContainer
			{
				Pattern: iscombinatorial = true
				RelevantResource: haveselectmethods = true
			}
		}

		if(haveselectmethods && iscombinatorial)
			for(e : pat.eAllContents.toList.filter(typeof(PatternSelectMethod)))
			{
				switch e.eContainer
				{
					RelevantResource:
						error("Pattern " + (pat as Pattern).name + " already uses combinational approach for relevant resources search",
							e.eContainer, RdoPackage.eINSTANCE.relevantResource_Selectmethod)}
			}
	}

	@Check
	def checkTableParameters(FunctionTable fun)
	{
		if(fun.parameters == null)
			return;

		for(p : fun.parameters)
		{
			val actual = p.type.resolveAllTypes
			if(!(actual instanceof RDOEnum || (actual instanceof RDOInt && (actual as RDOInt).range != null)))
				error("Invalid parameter type. Table function allows enumerative and ranged integer parameters only",
					p, RdoPackage.eINSTANCE.functionParameter_Type)
		}
	}

	@Check
	def checkSearchActivities(DecisionPointSearch search)
	{
		for (a : search.activities)
			if (a.pattern.type != PatternType.RULE)
				error("Only rules are allowed as search activities",
					a, RdoPackage.eINSTANCE.decisionPointSearchActivity_Name)
	}

	@Check
	def checkDptInitStatements(DecisionPointSome dpt)
	{
		var setCondStatements = dpt.initStatements.filter(
				s | s instanceof DptSetConditionStatement
			)
		var setPriorStatements = dpt.initStatements.filter(
				s | s instanceof DptSetPriorityStatement
			)
		var setParentStatements = dpt.initStatements.filter(
				s | s instanceof DptSetParentStatement
			)

		if (setCondStatements.size > 1)
			for (s : setCondStatements)
				error("Multiple setCondition() calls are not allowed",
						s, s.getNameStructuralFeature)

		if (setPriorStatements.size > 1)
			for (s : setPriorStatements)
				error("Multiple setPriority() calls are not allowed",
						s, s.getNameStructuralFeature)

		if (setParentStatements.size > 1)
			for (s : setParentStatements)
				error("Multiple setParent() calls are not allowed",
						s, s.getNameStructuralFeature)
	}

	@Check
	def checkSearchInitStatements(DecisionPointSearch dpt)
	{
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

		if (setCondStatements.size > 1)
			for (s : setCondStatements)
				error("Multiple setCondition() calls are not allowed",
						s, s.getNameStructuralFeature)

		if (setParentStatements.size > 1)
			for (s : setParentStatements)
				error("Multiple setParent() calls are not allowed",
						s, s.getNameStructuralFeature)

		if (setTerminateConditionStatements.size > 1)
			for (s : setTerminateConditionStatements)
				error("Multiple setTerminateCondition() calls are not allowed",
						s, s.getNameStructuralFeature)
		else if (setTerminateConditionStatements.empty)
				error("setTerminateCondition() method must be called explicitly",
						dpt, dpt.getNameStructuralFeature)

		if (evaluateByStatements.size > 1)
			for (s : evaluateByStatements)
				error("Multiple evaluateBy() calls are not allowed",
						s, s.getNameStructuralFeature)
		else if (evaluateByStatements.empty)
				error("evaluateBy() method must be called explicitly",
						dpt, dpt.getNameStructuralFeature)

		if (compareTopsStatements.size > 1)
			for (s : compareTopsStatements)
				error("Multiple compareTops() calls are not allowed",
						s, s.getNameStructuralFeature)
		else if (compareTopsStatements.empty)
				error("compareTops() method must be called explicitly",
						dpt, dpt.getNameStructuralFeature)
	}
}
