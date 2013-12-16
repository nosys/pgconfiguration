/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;


import es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Path(ServiceConfiguration.WS_PATH)
public class RootIndex {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] rootIndex() {
       return IndexUtil.generateIndex("{filename}");
    }
}