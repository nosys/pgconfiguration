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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@NotThreadSafe
@Path(ServiceConfiguration.WS_FILENAME_PATH)
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

    private void persist(Configuration configuration) throws IOException {
        File tempFile = pgData.resolve(FileUtils.getTempPersistFileName(ServiceConfiguration.POSTGRESQL_JSON)).toFile();
        ObjectMapper mapper = new ObjectMapperResolver().getContext(Configuration.class);
        mapper.writeValue(tempFile, configuration);

        FileUtils.moveFileAtomically(
                persistLock, tempFile.toPath(), pgData.resolve(ServiceConfiguration.POSTGRESQL_JSON)
        );
    }

    public void persist() throws IOException {
        persist(configuration);
    }

    @GET @Path("/pgdata")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPGData() {
        return pgData.toAbsolutePath().toString();
    }

    public java.nio.file.Path getPGDataAsPath() {
        return pgData;
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

    public Collection<Param> getPostgresqlconfParams() {
        return postgresqlconfParamByName.values();
    }

    public Map<String, Param> getPostgresqlconfParamByName() {
        return postgresqlconfParamByName;
    }

    public Map<String, List<Param>> getPostgresqlconfparamsByCategory() {
        return postgresqlconfparamsByCategory;
    }
}
