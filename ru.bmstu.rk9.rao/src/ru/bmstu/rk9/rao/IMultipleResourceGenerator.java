package ru.bmstu.rk9.rao;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;

public interface IMultipleResourceGenerator extends IGenerator {
	/**
	 * @param input
	 *            - the input for which to generate resources
	 * @param fsa
	 *            - file system access to be used to generate files
	 */
	public void doGenerate(ResourceSet input, IFileSystemAccess fsa);
}