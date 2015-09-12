package ru.bmstu.rk9.rao.ui.outline

import org.eclipse.swt.graphics.Image

import org.eclipse.xtext.ui.editor.outline.impl.AbstractOutlineNode
import org.eclipse.xtext.ui.editor.outline.IOutlineNode

import ru.bmstu.rk9.rao.rao.Parameter

import ru.bmstu.rk9.rao.rao.ResourceCreateStatement

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Function
import ru.bmstu.rk9.rao.rao.FunctionParameter

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RelevantResource
import ru.bmstu.rk9.rao.rao.DecisionPointSearchActivity
import ru.bmstu.rk9.rao.rao.DecisionPointActivity

import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Event

public class VirtualOutlineNode extends AbstractOutlineNode {
	protected new(IOutlineNode parent, Image image, Object text, boolean isLeaf) {
		super(parent, image, text, isLeaf)
	}
}

class RaoOutlineTreeProvider extends org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider {

	// Resource Types
	def _createChildren(IOutlineNode parentNode, ResourceType resourceType) {
		for (parameter : resourceType.eAllContents.toIterable.filter(typeof(Parameter))) {
			createNode(parentNode, parameter)
		}
	}

	def _isLeaf(Parameter parameter) { true }

	// Resources
	def _isLeaf(ResourceCreateStatement resourceCreateStatement) { true }

	// Sequence
	def _isLeaf(Sequence sequence) { true }

	// Functions
	def _createChildren(IOutlineNode parentNode, Function function) {
		for (functionParameter : function.eAllContents.toIterable.filter(typeof(FunctionParameter))) {
			createNode(parentNode, functionParameter)
		}
	}

	def _isLeaf(FunctionParameter functionParameter) { true }

	// Constants
	def _isLeaf(Constant constant) { true }

	// Pattern
	def _createChildren(IOutlineNode parentNode, Pattern pattern) {
		if (!pattern.eAllContents.filter(typeof(Parameter)).empty) {
			val groupParameters = new VirtualOutlineNode(parentNode, parentNode.image, "Parameters", false)
			for (parameter : pattern.eAllContents.toIterable.filter(typeof(Parameter))) {
				createEObjectNode(groupParameters, parameter)
			}
		}

		val groupRelevantResource = new VirtualOutlineNode(parentNode, parentNode.image, "Relevant resources", false)
		for (relevantResource : pattern.eAllContents.toIterable.filter(typeof(RelevantResource))) {
			createEObjectNode(groupRelevantResource, relevantResource)
		}
	}

	// Events
	def _isLeaf(Event event) { true }

	// Decision points
	def _isLeaf(DecisionPointSearchActivity decisionPointSearchActivity) { true }

	def _isLeaf(DecisionPointActivity decisionPointActivity) { true }

	// Results
	def _isLeaf(Result result) { true }

	// DefaultMethods
	def _isLeaf(DefaultMethod method) { true }
}
