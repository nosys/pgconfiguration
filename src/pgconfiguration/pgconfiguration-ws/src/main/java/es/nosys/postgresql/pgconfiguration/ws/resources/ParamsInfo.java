/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;


import es.nosys.postgresql.pgconfiguration.ws.PGConfiguration;
import es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Path(ServiceConfiguration.WS_FILENAME_PATH)
public class ParamsInfo {
    @Inject
    PGConfiguration pgConfiguration;

    @GET
    @Path(ServiceConfiguration.WS_PARAMS_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> params(@QueryParam("category") String category) {
        return null == category ? pgConfiguration.getParamNames() : pgConfiguration.getParamsByCategory(category);
    }

    @GET
    @Path(ServiceConfiguration.WS_CATEGORIES_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> categories() {
        return pgConfiguration.getParamCategories();
    }
}