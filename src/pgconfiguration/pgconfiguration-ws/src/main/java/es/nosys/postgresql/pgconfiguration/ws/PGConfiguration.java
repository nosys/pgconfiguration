/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import es.nosys.postgresql.pgconfiguration.model.Configuration;
import es.nosys.postgresql.pgconfiguration.model.Param;
import net.jcip.annotations.NotThreadSafe;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@NotThreadSafe
@Path(ServiceConfiguration.WS_PATH)
public class PGConfiguration {
    private static final String PGCONFIGURATION_JSON_JAR_RESOURCE = "pgconfiguration.json";

    private final Logger logger = LoggerFactory.getLogger(PGConfiguration.class);

    private final java.nio.file.Path pgData;
    private final Configuration configuration;
    private final Map<String,Param> postgresqlconfParamByName;
    private final Map<String,List<Param>> postgresqlconfparamsByCategory;
    private final Lock persistLock = new ReentrantLock();

    public PGConfiguration(java.nio.file.Path pgData) throws RuntimeException {
        this.pgData = pgData;
        configuration = readConfiguration(pgData);
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

    private Configuration readConfiguration(java.nio.file.Path pgData) throws RuntimeException {
        java.nio.file.Path postgresqlJson = pgData.resolve(ServiceConfiguration.POSTGRESQL_JSON);
        boolean postgresqlJsonExists = Files.exists(postgresqlJson);

        Configuration configuration;
        ObjectMapper objectMapper = new ObjectMapper();
        try(
                InputStream is = (! postgresqlJsonExists)
                        ? PGConfigurationServer.class.getClassLoader()
                            .getResourceAsStream(PGCONFIGURATION_JSON_JAR_RESOURCE)
                        : Files.newInputStream(postgresqlJson, StandardOpenOption.READ)
        ) {
            configuration = objectMapper.readValue(is, Configuration.class);
        } catch(JsonParseException e) {
            String error = ServiceConfiguration.POSTGRESQL_JSON + " format is invalid";
            logger.error(error, e);
            throw new RuntimeException(error);
        } catch (IOException e) {
            String error = "Error reading the "
                    + (postgresqlJsonExists ?
                        ServiceConfiguration.POSTGRESQL_JSON + " file"
                        : " internal " + PGCONFIGURATION_JSON_JAR_RESOURCE + " resource"
                    )
            ;
            logger.error(error, e);
            throw new RuntimeException(error);
        }

        if(! postgresqlJsonExists) {
            logger.info("Generating a new " + ServiceConfiguration.POSTGRESQL_JSON + " file");
            try {
                persist(configuration);
            } catch (IOException e) {
                String error = "Error writing a new " + ServiceConfiguration.POSTGRESQL_JSON + " file";
                logger.error(error, e);
                throw new RuntimeException(error);
            }
        }

        return configuration;
    }

    private String getTempPersistFileName() {
        return ServiceConfiguration.POSTGRESQL_JSON + "." + ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    }

    private File getTempPersistFile() {
        return pgData.resolve(getTempPersistFileName()).toFile();
    }

    private void persist(Configuration configuration) throws IOException {
        ObjectMapper mapper = new ObjectMapperResolver().getContext(Configuration.class);
        File tempFile = getTempPersistFile();
        mapper.writeValue(tempFile, configuration);

        persistLock.lock();
        try {
            try {
                Files.move(
                        tempFile.toPath(), pgData.resolve(ServiceConfiguration.POSTGRESQL_JSON),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
                );
            } catch(IOException e) {
                logger.error("Error persisting the " + ServiceConfiguration.POSTGRESQL_JSON + " file", e);
                throw e;
            } finally {
                if(tempFile.exists()) {
                    try {
                        Files.delete(tempFile.toPath());
                    } catch(IOException e) {
                        logger.error("Cannot delete temp file " + tempFile.getName(), e);
                        throw e;
                    }
                }
            }
        } finally {
            persistLock.unlock();
        }
    }

    public void persist() throws IOException {
        persist(configuration);
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
     * @throws java.io.IOException If there is a problem persisting the configuration file
     */
    // TODO: add param validation
    public Param setParam(String param, String value) throws IOException {
        Param p = getParamByName(param);
        if(null == p) {
            return null;
        }
        p.setValue(value);

        persist();

        return p;
    }
}
