package ru.bmstu.rk9.rao.thinclient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class EmbeddedThinClientServer {
	public static void startServer() throws Exception {
		Server server = new Server(9002);

		ContextHandler currentTimeContext = new ContextHandler("/time");
		currentTimeContext.setHandler(new CurrentTimeHandle());
		currentTimeContext.setAllowNullPathInfo(true);

		ContextHandler pingContext = new ContextHandler("/ping");
		pingContext.setHandler(new TestPingHandle());
		pingContext.setAllowNullPathInfo(true);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { currentTimeContext, pingContext });

		server.setHandler(contexts);
		server.start();

	}

}