/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Created: 12/16/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper = new ObjectMapper();

    public ObjectMapperResolver() {
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
