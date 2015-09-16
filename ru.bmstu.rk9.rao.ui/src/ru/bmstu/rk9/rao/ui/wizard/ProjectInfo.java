package ru.bmstu.rk9.rao.ui.wizard;

public class ProjectInfo {

	public enum TemplateType {NO_TEMPLATE, BARBER_SIMPLE, BARBER_EVENTS, BARBER_CLIENTS};
	private final String projectName;
	private final TemplateType template;
	
	public ProjectInfo(final String projectName, final TemplateType template) {
		this.projectName = projectName;
		this.template = template;
	}
	
	public final String getProjectName() {
		return projectName;
	}
	
	public final TemplateType getTemplate() {
		return template;
	}
}
