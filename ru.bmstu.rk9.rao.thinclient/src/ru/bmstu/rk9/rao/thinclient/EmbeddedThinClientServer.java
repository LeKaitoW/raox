package ru.bmstu.rk9.rao.thinclient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import ru.bmstu.rk9.rao.thinclient.handlers.CurrentTime;
import ru.bmstu.rk9.rao.thinclient.handlers.TestPingHandle;

public class EmbeddedThinClientServer {

	static Server server = new Server(9002);

	public static void startServer() throws Exception {

		ContextHandler currentTimeContext = createHandler(new CurrentTime(), "/currentTime");

		ContextHandler pingContext = createHandler(new TestPingHandle(), "/ping");

		ContextHandlerCollection handlers = new ContextHandlerCollection();
		handlers.setHandlers(new Handler[] { currentTimeContext, pingContext });

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