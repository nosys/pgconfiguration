/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Created: 12/14/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
public class Main {
    public static final int DEFAULT_PORT = 8543;

    private static final String WS_PACKAGE = Main.class.getPackage().getName();

    public static void main(String[] args) {
        Server server = new Server(DEFAULT_PORT);

        // Configure servlet environment
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        server.setHandler(servletContextHandler);

        // Jersey uses java.util.logging - bridge to slf4
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Configure jersey environment. Load REST WSs
        ServletHolder holder = new ServletHolder(new ServletContainer());
        holder.setInitParameter(
                "com.sun.jersey.config.property.packages", WS_PACKAGE + ";" + "org.codehaus.jackson.jaxrs"
        );
        servletContextHandler.addServlet(holder, "/*");

        // Launch server and join it to the main thread
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Server failed to start. Check logs for more information");
            System.exit(1);
        }

        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();     // Restore interrupt status
        }
    }
}
