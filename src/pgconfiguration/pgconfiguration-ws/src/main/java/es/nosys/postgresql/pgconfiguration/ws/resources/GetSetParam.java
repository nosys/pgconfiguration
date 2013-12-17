/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;


import es.nosys.postgresql.pgconfiguration.model.Param;
import es.nosys.postgresql.pgconfiguration.ws.PGConfiguration;
import es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Path(ServiceConfiguration.WS_FILENAME_PATH)
public class GetSetParam {
    @Inject
    PGConfiguration pgConfiguration;

    @GET
    @Path(ServiceConfiguration.WS_PARAM_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Param get(@PathParam("param") String param) {
       return pgConfiguration.getParamByName(param);
    }

    @PUT
    @Path(ServiceConfiguration.WS_PARAM_PATH)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Param set(@PathParam("param") String param, String value, @Context final HttpServletResponse response)
    throws IOException {
        try {
            return setParam(param, value);
        } catch (IOException e) {
            response.sendError(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Error persisting the " + ServiceConfiguration.POSTGRESQL_JSON + " file"
            );

            return null;
        }
    }

    /**
     *
     * @param param
     * @param value
     * @return The old value
     * @throws java.io.IOException If there is a problem persisting the configuration file
     */
    // TODO: add param validation
    private Param setParam(String param, String value) throws IOException {
        Param p = pgConfiguration.getParamByName(param);
        if(null == p) {
            return null;
        }
        p.setValue(value);

        pgConfiguration.persist();

        return p;
    }
}