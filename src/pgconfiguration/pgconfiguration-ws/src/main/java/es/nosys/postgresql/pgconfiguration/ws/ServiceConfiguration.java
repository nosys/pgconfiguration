/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws;

import net.jcip.annotations.Immutable;

/**
 * Created: 12/15/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
@Immutable
public final class ServiceConfiguration {
    public static final int DEFAULT_PORT = 8543;
    public static final String WEBAPP_FOLDER = "webapp";
    public static final String WELCOME_FILE = "index.html";

    public static final String POSTGRESQL_CONF = "postgresql.conf";
    public static final String POSTGRESQL_JSON = "postgresql.json";

    public static final String WS_PATH = "/ws";
    public static final String WS_FILENAME = "/{filename: postgresql.conf}";
    public static final String WS_FILENAME_PATH = WS_FILENAME;
    public static final String WS_PARAM_PATH = "/param/{param}";
    public static final String WS_PARAMS_PATH = "/params";
    public static final String WS_CATEGORIES_PATH = "/categories";
    public static final String WS_DUMP_PATH = "/dump";
    public static final String WS_SAVE_PATH = "/save";
    public static final String WS_SEARCH_PATH = "/search/{searchstring}";

    private ServiceConfiguration() {}
}
