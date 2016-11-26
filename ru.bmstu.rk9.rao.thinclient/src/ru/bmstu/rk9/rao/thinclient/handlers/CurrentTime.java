package ru.bmstu.rk9.rao.thinclient.handlers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.runtime.RaoRuntime;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class CurrentTime extends AbstractHandler {

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		JsonObject currentTime = new JsonObject();
		currentTime.addProperty("currentTime", RaoRuntime.getCurrentTime());

		PrintWriter writer = response.getWriter();
		if (CurrentSimulator.isRunning() == true)
			writer.println(currentTime);
		else
			writer.println("Модель не запущена");

		baseRequest.setHandled(true);
	}
}