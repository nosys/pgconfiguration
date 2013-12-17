/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created: 12/14/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static void usage() {
        System.err.println("\nUsage:\tjava -jar <pgconfiguration-resources-x.yz.jar> [PGDATA]\n");
        System.err.println("PGDATA is the (relative or absolute) directory containing the postgresql.conf file");
        System.err.println("(defaults to the current directory if not specified).\n");
    }

    private static void exitWithError(String error, int exitCode) {
        System.err.println(error);
        usage();
        System.exit(exitCode);
    }

    private static boolean pgDataIsValid(Path pgData) {
        return Files.isWritable(pgData.resolve(ServiceConfiguration.POSTGRESQL_CONF));
    }

    public static void main(String[] args) {
        Path pgData = Paths.get(args.length == 0 ? "" : args[0]);
        if(! Files.exists(pgData)) {
            exitWithError("Directory " + pgData.toAbsolutePath() + " does not exist", 1);
        }
        if(! pgDataIsValid(pgData)) {
            exitWithError(
                    "Directory " + pgData.toAbsolutePath() + " does not contain the file "
                            + ServiceConfiguration.POSTGRESQL_CONF + " or it's not writable"
                    , 2
            );
        }

        PGConfigurationServer pgConfigurationServer = new PGConfigurationServer(pgData);

        try {
            pgConfigurationServer.run();
        } catch (Exception e) {
            String errorMessage = "Server failed to start. Check the logs for more information";
            LOGGER.error(errorMessage, e);
            System.err.println(errorMessage);
            System.exit(1);
        }
    }
}
