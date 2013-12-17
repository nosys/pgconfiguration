/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;


import es.nosys.postgresql.pgconfiguration.model.Param;
import es.nosys.postgresql.pgconfiguration.ws.PGConfiguration;
import es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        return null == category ? pgConfiguration.getParamNames() : getParamsByCategory(category);
    }

    @GET
    @Path(ServiceConfiguration.WS_CATEGORIES_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> categories() {
        return pgConfiguration.getParamCategories();
    }

    public Set<String> getParamsByCategory(String category) {
        Map<String,List<Param>> postgresqlconfparamsByCategory = pgConfiguration.getPostgresqlconfparamsByCategory();
        if(category == null || ! postgresqlconfparamsByCategory.containsKey(category)) {
            return null;
        }

        List<Param> params = postgresqlconfparamsByCategory.get(category);
        Set<String> paramNames = new TreeSet<String>();
        for(Param param : params) {
            paramNames.add(param.getParam());
        }

        return paramNames;
    }
}