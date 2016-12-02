package ru.bmstu.rk9.rao.thinclient;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.xml.XmlConfiguration;

import ru.bmstu.rk9.rao.thinclient.handlers.CurrentTime;
import ru.bmstu.rk9.rao.thinclient.handlers.Models;
import ru.bmstu.rk9.rao.thinclient.handlers.Ping;

public class EmbeddedThinClientServer {

	static Server server = new Server();

	public static void startServer() throws Exception {

		XmlConfiguration configuration = new XmlConfiguration(
				new File("../raox/ru.bmstu.rk9.rao.thinclient/jetty/jetty.xml").toURI().toURL());
		configuration.configure(server);

		ContextHandler currentTimeContext = createHandler(new CurrentTime(), "/currentTime");

		ContextHandler pingContext = createHandler(new Ping(), "/ping");

		ContextHandler modelsContext = createHandler(new Models(), "/models");

		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.setHandlers(new Handler[] { currentTimeContext, pingContext, modelsContext });

		server.setHandler(handlers);
		server.start();
	}

	static private ContextHandler createHandler(AbstractHandler handlerClass, String handlerName) {

		ContextHandler builtHandler = new ContextHandler(handlerName);
		builtHandler.setHandler(handlerClass);
		builtHandler.setAllowNullPathInfo(true);
		return builtHandler;

	}

	public static void stopServer() throws Exception {

		server.stop();

	}

}