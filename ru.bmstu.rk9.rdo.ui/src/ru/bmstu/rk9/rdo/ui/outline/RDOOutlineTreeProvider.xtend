package ru.bmstu.rk9.rdo.ui.outline

import org.eclipse.swt.graphics.Image

import org.eclipse.xtext.ui.editor.outline.impl.AbstractOutlineNode
import org.eclipse.xtext.ui.editor.outline.IOutlineNode

import ru.bmstu.rk9.rdo.rdo.ParameterType

import ru.bmstu.rk9.rdo.rdo.ResourceCreateStatement

import ru.bmstu.rk9.rdo.rdo.Sequence

import ru.bmstu.rk9.rdo.rdo.Function
import ru.bmstu.rk9.rdo.rdo.FunctionParameter

import ru.bmstu.rk9.rdo.rdo.Constant

import ru.bmstu.rk9.rdo.rdo.Pattern
import ru.bmstu.rk9.rdo.rdo.RelevantResource
import ru.bmstu.rk9.rdo.rdo.DecisionPointSearchActivity
import ru.bmstu.rk9.rdo.rdo.DecisionPointActivity

import ru.bmstu.rk9.rdo.rdo.Result
import ru.bmstu.rk9.rdo.rdo.DefaultMethod

public class VirtualOutlineNode extends AbstractOutlineNode
{
	protected new(IOutlineNode parent, Image image, Object text, boolean isLeaf)
	{
		super(parent, image, text, isLeaf)
	}
}

class RDOOutlineTreeProvider extends org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider
{
	// Resource Types
	def _isLeaf(ParameterType rtp) { true }

	// Resources
	def _isLeaf(ResourceCreateStatement rss) { true }

	// Sequence
	def _isLeaf(Sequence seq) { true }

	// Functions
	def _createChildren(IOutlineNode parentNode, Function fun)
	{
		for(e : fun.eAllContents.toIterable.filter(typeof(FunctionParameter)))
		{
			createNode(parentNode, e)
		}
	}
	def _isLeaf(FunctionParameter p) { true }

	// Constants
	def _isLeaf (Constant c) { true }

	// Pattern
	def _createChildren(IOutlineNode parentNode, Pattern pat)
	{
		if(!pat.eAllContents.filter(typeof(ParameterType)).empty)
		{
			val groupParameters = new VirtualOutlineNode(parentNode, parentNode.image, "Parameters", false)
			for(p : pat.eAllContents.toIterable.filter(typeof(ParameterType)))
			{
				createEObjectNode(groupParameters, p)
			}
		}


		val groupRelRes = new VirtualOutlineNode(parentNode, parentNode.image, "Relevant resources", false)
		for(r : pat.eAllContents.toIterable.filter(typeof(RelevantResource)))
		{
			createEObjectNode(groupRelRes, r)
		}
	}

	// Decision points
	def _isLeaf(DecisionPointSearchActivity d) { true }
	def _isLeaf(DecisionPointActivity d)       { true }

	// Results
	def _isLeaf(Result r) { true }

	// OnInit
	def _isLeaf(DefaultMethod method) { true }
}
