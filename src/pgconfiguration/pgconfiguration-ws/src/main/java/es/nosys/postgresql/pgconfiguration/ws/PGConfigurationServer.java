/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import es.nosys.postgresql.pgconfiguration.model.Configuration;
import es.nosys.postgresql.pgconfiguration.ws.resources.GetParam;
import net.jcip.annotations.NotThreadSafe;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Created: 12/14/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@NotThreadSafe      // Exports object (PGConfiguration) which may be mutated externally in an unsafe manner
public class PGConfigurationServer {
    private final Path pgData;
    private final Configuration configuration;

    public PGConfigurationServer(Path pgData) {
        this.pgData = pgData;

        InputStream is = PGConfigurationServer.class.getClassLoader().getResourceAsStream("pgconfiguration.json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            configuration = objectMapper.readValue(is, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the included pgconfiguration.json resource", e);
        }
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
                .register(new PGConfiguration(pgData, configuration))       // Manually inject dependency
                .packages(GetParam.class.getPackage().getName());           // Register WS resources
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
