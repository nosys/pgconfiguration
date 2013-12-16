/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import es.nosys.postgresql.pgconfiguration.ws.resources.*;
import net.jcip.annotations.NotThreadSafe;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.nio.file.Path;

/**
 * Created: 12/14/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@NotThreadSafe      // Exports object (PGConfiguration) which may be mutated externally in an unsafe manner
public class PGConfigurationServer {
    private final Path pgData;

    public PGConfigurationServer(Path pgData) {
        this.pgData = pgData;
    }

    public void run() throws Exception {
        Server server = new Server(ServiceConfiguration.DEFAULT_PORT);

        // Configure servlet environment
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        server.setHandler(servletContextHandler);

        // Jersey uses java.util.logging - bridge to slf4
        // http://blog.cn-consult.dk/2009/03/bridging-javautillogging-to-slf4j.html
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Configure jersey environment
        ResourceConfig rc = new ResourceConfig()
                .packages("org.glassfish.jersey.examples.jackson")          // Register Jackson
                .register(JacksonFeature.class)
                .register(ObjectMapperResolver.class)
                .register(new PGConfiguration(pgData))      // Manually inject dependency
                .register(FileOperations.class)             // Register each WS class individually
                .register(GetSetParam.class)
                .register(ParamsInfo.class)
                .register(RootIndex.class)
                .register(FilenameIndex.class);
        servletContextHandler.addServlet(new ServletHolder(new ServletContainer(rc)), "/*");

        // Launch server and join it to the main thread
        server.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();     // Restore interrupt status
        }
    }
}
