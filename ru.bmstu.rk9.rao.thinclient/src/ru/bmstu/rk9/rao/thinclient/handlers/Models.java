package ru.bmstu.rk9.rao.thinclient.handlers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Models extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("application/json; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		JsonArray models = new JsonArray();

		for (IProject project : projectsInWorkspace) {
			JsonObject projectJson = new JsonObject();
			projectJson.addProperty("name", project.getName());
			models.add(projectJson);
		}

		JsonObject modelsJson = new JsonObject();
		modelsJson.add("models", models);

		PrintWriter writer = response.getWriter();
		writer.println(modelsJson);

		baseRequest.setHandled(true);
	}
}