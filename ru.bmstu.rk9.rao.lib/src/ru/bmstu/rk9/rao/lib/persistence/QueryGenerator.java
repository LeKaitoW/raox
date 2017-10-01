package ru.bmstu.rk9.rao.lib.persistence;

import java.io.File;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.querydsl.codegen.GenericExporter;
import com.querydsl.codegen.Keywords;

//Code generation http://www.querydsl.com/static/querydsl/3.3.0/reference/html/ch03s03.html
public final class QueryGenerator {

	private final GenericExporter exporter;

	public QueryGenerator(ClassLoader classLoader, File exportPath) {
		exporter = new GenericExporter(classLoader);
		exporter.setKeywords(Keywords.JPA);
		exporter.setEntityAnnotation(Entity.class);
		exporter.setEmbeddableAnnotation(Embeddable.class);
		exporter.setEmbeddedAnnotation(Embedded.class);
		exporter.setSupertypeAnnotation(MappedSuperclass.class);
		exporter.setSkipAnnotation(Transient.class);
		exporter.setTargetFolder(exportPath);
	}

	public void generate(Class<?> domainClass) {
		exporter.export(domainClass);
	}
}
