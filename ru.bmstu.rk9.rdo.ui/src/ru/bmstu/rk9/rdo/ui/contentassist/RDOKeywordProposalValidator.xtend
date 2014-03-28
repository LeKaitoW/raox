package ru.bmstu.rk9.rdo.ui.contentassist

import org.eclipse.emf.ecore.EObject

import ru.bmstu.rk9.rdo.rdo.ResourceType

import ru.bmstu.rk9.rdo.rdo.ResourceDeclaration

import ru.bmstu.rk9.rdo.rdo.EventRelevantResource


class RDOKeywordProposalValidator
{
	def public static boolean check(String keyword, EObject context)
	{
		switch context
		{
			EventRelevantResource:
			{
				switch context.type
				{
					ResourceType:
						if(keyword != "Create")
							return false

					ResourceDeclaration:
						if(keyword != "Keep")
							return false
				}
			}
		}
		return true
	}
}
