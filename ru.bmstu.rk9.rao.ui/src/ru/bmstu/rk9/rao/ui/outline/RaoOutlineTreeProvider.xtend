package ru.bmstu.rk9.rao.ui.outline

import org.eclipse.swt.graphics.Image

import org.eclipse.xtext.ui.editor.outline.impl.AbstractOutlineNode
import org.eclipse.xtext.ui.editor.outline.IOutlineNode

import ru.bmstu.rk9.rao.rao.Sequence

import ru.bmstu.rk9.rao.rao.Constant

import ru.bmstu.rk9.rao.rao.Pattern
import ru.bmstu.rk9.rao.rao.RelevantResource

import ru.bmstu.rk9.rao.rao.Result
import ru.bmstu.rk9.rao.rao.DefaultMethod
import ru.bmstu.rk9.rao.rao.ResourceType
import ru.bmstu.rk9.rao.rao.Event
import ru.bmstu.rk9.rao.rao.FunctionDeclaration
import ru.bmstu.rk9.rao.rao.FieldDeclaration

public class VirtualOutlineNode extends AbstractOutlineNode {
	protected new(IOutlineNode parent, Image image, Object text, boolean isLeaf) {
		super(parent, image, text, isLeaf)
	}
}

class RaoOutlineTreeProvider extends org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider {

	// Resource Types
	def _createChildren(IOutlineNode parentNode, ResourceType resourceType) {
		for (parameter : resourceType.eAllContents.toIterable.filter(typeof(FieldDeclaration))) {
			createNode(parentNode, parameter)
		}
	}

	def _isLeaf(FieldDeclaration field) { true }

	// Sequence
	def _isLeaf(Sequence sequence) { true }

	// Functions
	def _isLeaf(IOutlineNode parentNode, FunctionDeclaration function) { true }

	// Constants
	def _isLeaf(Constant constant) { true }

	// Pattern
	def _createChildren(IOutlineNode parentNode, Pattern pattern) {
		if (!pattern.eAllContents.filter(typeof(FieldDeclaration)).empty) {
			val groupParameters = new VirtualOutlineNode(parentNode, parentNode.image, "Parameters", false)
			for (fieldDeclaration : pattern.eAllContents.toIterable.filter(typeof(FieldDeclaration))) {
				createEObjectNode(groupParameters, fieldDeclaration)
			}
		}

		val groupRelevantResource = new VirtualOutlineNode(parentNode, parentNode.image, "Relevant resources", false)
		for (relevantResource : pattern.eAllContents.toIterable.filter(typeof(RelevantResource))) {
			createEObjectNode(groupRelevantResource, relevantResource)
		}
	}

	// Events
	def _isLeaf(Event event) { true }

	// Results
	def _isLeaf(Result result) { true }

	// DefaultMethods
	def _isLeaf(DefaultMethod method) { true }
}
