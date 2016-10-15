package ru.bmstu.rk9.rao.ui.thinclient;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class TestPingHandle extends AbstractHandler
{

	public void handle( String target,
                    Request baseRequest,
                    HttpServletRequest request,
                    HttpServletResponse response ) throws IOException, ServletException
	{
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();

		out.println("200");
    
		baseRequest.setHandled(true);
}
}