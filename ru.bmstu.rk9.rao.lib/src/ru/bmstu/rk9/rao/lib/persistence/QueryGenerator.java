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
//QueryDSL with no generation https://stackoverflow.com/questions/15135572/is-it-possible-to-use-querydsl-without-generated-query-types
public class QueryGenerator {
	public static void generate(Class<?> domainClass, File exportPath, URLClassLoader classLoader) {
		exportPath.mkdir();
		GenericExporter exporter = new GenericExporter(classLoader);
		exporter.setKeywords(Keywords.JPA);
		// Данные обявления нужны, иначе будут использованы свои аннотации от QueryDSL,
		// чего не надо
		exporter.setEntityAnnotation(Entity.class);
		exporter.setEmbeddableAnnotation(Embeddable.class);
		exporter.setEmbeddedAnnotation(Embedded.class);
		exporter.setSupertypeAnnotation(MappedSuperclass.class);
		exporter.setSkipAnnotation(Transient.class);
		exporter.setTargetFolder(exportPath);
		exporter.export(domainClass);
		// TODO Проверка на существование файла
		Set<File> files = exporter.getGeneratedFiles();
		for (File file : files) {
			System.out.println(file.exists());
		}
	}
}
