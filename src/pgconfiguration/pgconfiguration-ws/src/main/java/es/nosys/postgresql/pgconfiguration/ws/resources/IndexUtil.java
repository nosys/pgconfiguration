/*
 * Copyright (c) 2009-2013, Networked Open SYStems S.L. (NOSYS)
 */

package es.nosys.postgresql.pgconfiguration.ws.resources;

import static es.nosys.postgresql.pgconfiguration.ws.ServiceConfiguration.*;

/**
 * Created: 12/16/13
 *
 * @author Álvaro Hernández Tortosa <aht@nosys.es>
 */
public class IndexUtil {
    public static String[] generateIndex(String filename) {
        final String basePath = WS_PATH + "/" + filename;

        return new String[] {
                WS_PATH, basePath + WS_PARAMS_PATH, basePath + WS_CATEGORIES_PATH, basePath + WS_PARAM_PATH,
                basePath + WS_DUMP_PATH, basePath + WS_SAVE_PATH
        };
    }
}
