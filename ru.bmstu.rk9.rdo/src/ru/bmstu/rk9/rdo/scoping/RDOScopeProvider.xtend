package ru.bmstu.rk9.rdo.scoping

import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes

import org.eclipse.emf.ecore.EReference

import ru.bmstu.rk9.rdo.rdo.Resources
import ru.bmstu.rk9.rdo.rdo.ResourceTrace

class RDOScopeProvider extends AbstractDeclarativeScopeProvider {

	def IScope scope_ResourceTrace_trace(ResourceTrace context, EReference ref)
	{
		return Scopes.scopeFor((context.eContainer as Resources).resources)
	}

}
