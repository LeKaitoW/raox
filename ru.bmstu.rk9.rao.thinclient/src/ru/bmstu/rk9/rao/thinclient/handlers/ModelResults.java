package ru.bmstu.rk9.rao.thinclient.handlers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class ModelResults extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("application/json; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		JsonArray modelResults = new JsonArray();
		List<Result<?>> results = CurrentSimulator.getResults();
		Gson gson = new Gson();

		for (Result<?> result : results) {
			modelResults.add(gson.toJson(result.getData()));
		}

		JsonObject modelResultsJson = new JsonObject();
		modelResultsJson.add("modelResults", modelResults);
		PrintWriter writer = response.getWriter();
		writer.println(modelResultsJson);

		baseRequest.setHandled(true);
	}
}
