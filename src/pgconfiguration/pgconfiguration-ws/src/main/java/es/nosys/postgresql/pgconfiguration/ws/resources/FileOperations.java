/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;


import es.nosys.postgresql.pgconfiguration.ws.FileUtils;
import es.nosys.postgresql.pgconfiguration.ws.PGConfiguration;
import es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Path(ServiceConfiguration.WS_FILENAME_PATH)
public class FileOperations {
    private static final Lock PERSIST_LOCK = new ReentrantLock();

    @Inject
    PGConfiguration pgConfiguration;

    @GET
    @Path(ServiceConfiguration.WS_DUMP_PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public String dump() {
        return pgConfiguration.toKeyValueFile();
    }

    @GET
    @Path(ServiceConfiguration.WS_SAVE_PATH)
    @Produces(MediaType.TEXT_PLAIN)
    public String save(@Context final HttpServletResponse response) throws IOException {
        String postgresqlConf = pgConfiguration.toKeyValueFile();
        try {
            save(postgresqlConf);
        } catch (IOException e) {
            response.sendError(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Error persisting the " + ServiceConfiguration.POSTGRESQL_CONF + " file"
            );
        }

        return postgresqlConf;
    }

    private void save(String postgresqlConf) throws IOException {
        java.nio.file.Path pgData = pgConfiguration.getPGDataAsPath();
        File tempFile = pgData.resolve(FileUtils.getTempPersistFileName(ServiceConfiguration.POSTGRESQL_CONF)).toFile();
        try(PrintWriter pw = new PrintWriter(tempFile)) {
            pw.write(postgresqlConf);
        }

        java.nio.file.Path postgresqlConfPath = pgData.resolve(ServiceConfiguration.POSTGRESQL_CONF);
        java.nio.file.Path postgresqlConfBackupPath = pgData.resolve(
                ServiceConfiguration.POSTGRESQL_CONF + "." + new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date())
        );

        // Save a copy of the curren postgresqlconf file
        FileUtils.moveFileAtomically(PERSIST_LOCK, postgresqlConfPath, postgresqlConfBackupPath);

        // Move the new file to the final path name
        FileUtils.moveFileAtomically(PERSIST_LOCK, tempFile.toPath(), postgresqlConfPath);
    }
}