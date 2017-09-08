package ru.bmstu.rk9.rao.lib.persistence;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.querydsl.codegen.GenericExporter;
import com.querydsl.codegen.Keywords;

//Code generation http://www.querydsl.com/static/querydsl/3.3.0/reference/html/ch03s03.html
public class QueryGenerator {
	public static void generate(Class<?> domainClass, File exportPath, URLClassLoader classLoader) {
		exportPath.mkdir();
		GenericExporter exporter = new GenericExporter(classLoader);
		exporter.setKeywords(Keywords.JPA);
		// TODO Удостовериться в нужности данных методов
		exporter.setEntityAnnotation(Entity.class);
		exporter.setEmbeddableAnnotation(Embeddable.class);
		exporter.setEmbeddedAnnotation(Embedded.class);
		exporter.setSupertypeAnnotation(MappedSuperclass.class);
		exporter.setSkipAnnotation(Transient.class);
		exporter.setTargetFolder(exportPath);
		exporter.export(domainClass);
		Set<File> files = exporter.getGeneratedFiles();
		for (File file : files) {
			System.out.println(file.exists());
		}
	}
}
