package ru.bmstu.rk9.rao.ui.thinclient;

import ru.bmstu.rk9.rao.lib.runtime.RaoRuntime;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class CurrentTimeHandle extends AbstractHandler
{

	public void handle( String target,
	                    Request baseRequest,
	                    HttpServletRequest request,
	                    HttpServletResponse response ) throws IOException, ServletException
	{
	    response.setContentType("text/html; charset=utf-8");
	    response.setStatus(HttpServletResponse.SC_OK);
	    PrintWriter out = response.getWriter();
	    if (CurrentSimulator.isRunning() == true)  out.println(RaoRuntime.getCurrentTime()); 
	    else out.println("сервер недоступен");
	    
	    baseRequest.setHandled(true);
	}
}