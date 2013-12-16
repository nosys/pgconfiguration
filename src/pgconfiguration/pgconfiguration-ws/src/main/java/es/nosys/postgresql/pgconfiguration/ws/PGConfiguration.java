/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import es.nosys.postgresql.pgconfiguration.model.Configuration;
import es.nosys.postgresql.pgconfiguration.model.Param;
import net.jcip.annotations.NotThreadSafe;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@NotThreadSafe
@Path(ServiceConfiguration.WS_PATH)
public class PGConfiguration {
    private final java.nio.file.Path pgData;
    private final Configuration configuration;
    private final Map<String,Param> postgresqlconfParamByName;
    private final Map<String,List<Param>> postgresqlconfparamsByCategory;

    public PGConfiguration(java.nio.file.Path pgData, Configuration configuration) {
        this.pgData = pgData;
        this.configuration = configuration;
        List<Param> postgresqlconfParams = configuration.getPostgresqlconf();
        postgresqlconfParamByName = new HashMap<>(postgresqlconfParams.size());
        postgresqlconfparamsByCategory = new HashMap<>();
        for(Param param : postgresqlconfParams) {
            postgresqlconfParamByName.put(param.getParam(), param);
            List<Param> paramsInCategory;
            if(! postgresqlconfparamsByCategory.containsKey(param.getCategory())) {
                paramsInCategory = new ArrayList<>();
                postgresqlconfparamsByCategory.put(param.getCategory(), paramsInCategory);
            } else {
                paramsInCategory = postgresqlconfparamsByCategory.get(param.getCategory());
            }
            paramsInCategory.add(param);
        }
    }

    @GET @Path("/pgdata")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPGData() {
        return pgData.toAbsolutePath().toString();
    }

    public Param getParamByName(String name) {
        return (name == null || ! postgresqlconfParamByName.containsKey(name))
                ? null : postgresqlconfParamByName.get(name);
    }

    public Set<String> getParamNames() {
        return postgresqlconfParamByName.keySet();
    }

    public Set<String> getParamCategories() {
        return postgresqlconfparamsByCategory.keySet();
    }

    public String[] getParamsByCategory(String category) {
        if(category == null || ! postgresqlconfparamsByCategory.containsKey(category)) {
            return null;
        }

        List<Param> params = postgresqlconfparamsByCategory.get(category);
        String[] paramNames = new String[params.size()];
        int i = 0;
        for(Param param : params) {
            paramNames[i++] = param.getParam();
        }

        return paramNames;
    }

    /**
     *
     * @param param
     * @param value
     * @return The old value
     */
    // TODO: add validation
    public String setParam(String param, String value) {
        Param p = getParamByName(param);
        if(null == p) {
            return null;
        }

        String oldValue = p.getValue();
        p.setValue(value);

        return oldValue;
    }
}
