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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
// TODO: improve the search method. Current implementation is a trivial, naive implementation just for POC demonstration
// TODO: use a search framework such as Lucene to improve search performance and funtionality
@Path(ServiceConfiguration.WS_FILENAME_PATH)
public class Search {
    @Inject
    PGConfiguration pgConfiguration;

    @GET
    @Path(ServiceConfiguration.WS_SEARCH_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Param> search(@PathParam("searchstring") String searchString) {
        if(null == searchString) {
            return null;
        }

        List<Param> params = new ArrayList<>();
        for(Param param : pgConfiguration.getPostgresqlconfParams()) {
            if(
                    param.getParam().contains(searchString) || param.getCategory().contains(searchString)
                    || (param.getDescription() != null && param.getDescription().contains(searchString))
                    || (param.getExtra() != null && param.getExtra().contains(searchString))
            ) {
                params.add(param);
            }
        }

        return params.size() > 0 ? params : null;
    }
}