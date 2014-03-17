package ru.bmstu.rk9.rdo.scoping

import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.eclipse.xtext.scoping.IScope

//import org.eclipse.emf.ecore.EReference
//import ru.bmstu.rk9.rdo.rdo.RDOModel
//import org.eclipse.xtext.scoping.IScope
//
//import ru.bmstu.rk9.rdo.rdo.RDORTPParameterEnum
//import ru.bmstu.rk9.rdo.rdo.RDOSuchAs
//import java.util.List
//import org.eclipse.emf.ecore.EObject
//import ru.bmstu.rk9.rdo.rdo.ResourceTypeParameter
//import ru.bmstu.rk9.rdo.rdo.ResourceType
//import ru.bmstu.rk9.rdo.rdo.ConstantDeclaration
//import ru.bmstu.rk9.rdo.rdo.RDORTPParameterSuchAs
//import java.util.ArrayList

class RDOScopeProvider extends AbstractDeclarativeScopeProvider {

//	def IScope scope_RDORTPParameterEnum_default (RDORTPParameterEnum context, EReference ref) {
//		val stub = ((context.eContainer.eContainer.eContainer) as RDOModel).eAllContents.toList
//		return Scopes.scopeFor(stub)
//	}
//	
//	def IScope scope_RDOSuchAs_type (RDOSuchAs context, EReference ref) {
//		
//		println ("============================================================")
//		println ("Scope call from " + (context.eContainer.eContainer.eContainer as ResourceType).name + "." + (context.eContainer.eContainer as ResourceTypeParameter).name )
//		println ("============================================================")
//		
//		val pars = ((context.eContainer.eContainer.eContainer.eContainer) as RDOModel).eAllContents.filter(typeof(ResourceTypeParameter)).toList
//		val cons = ((context.eContainer.eContainer.eContainer.eContainer) as RDOModel).eAllContents.filter(typeof(ConstantDeclaration)).toList
//
//		var List<EObject> list = new ArrayList<EObject>();
//		
//		switch context.eContainer {
//			RDORTPParameterSuchAs:
//				for (e : pars) {
//					println ("Checking " + (e.eContainer as ResourceType).name + "." + e.name )
//					if (e.name != (context.eContainer.eContainer as ResourceTypeParameter).name ||
//						(e.eContainer as ResourceType).name != (context.eContainer.eContainer.eContainer as ResourceType).name)
//						{
//							list.add(e)
//							println ("OK")
//						} else println ("NOPE")
//				}				
//		}
//		//list.addAll(pars)
//		list.addAll(cons)		
//				
//		return Scopes.scopeFor(list)		
//	}
	 

}
