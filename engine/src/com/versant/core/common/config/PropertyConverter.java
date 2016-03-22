
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.common.config;

import com.versant.core.util.StringListParser;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.JdoExtension;

import java.util.*;
import java.io.*;

/**
 * This class is used to convert old jdogenie properties
 * to new versant properties,
 */
public class PropertyConverter {

    public static final HashMap oldToNew = new HashMap();

    static {
        String PROJECT_DESCRIPTION = "project.description";
        oldToNew.put(PROJECT_DESCRIPTION, ConfigParser.PROJECT_DESCRIPTION);
        String SERVER = "server";
        oldToNew.put(SERVER, ConfigParser.SERVER);
        String HOST = "host";
        oldToNew.put(HOST, "versant.host");
        String ALLOW_REMOTE_ACCESS = "remote.access";
        oldToNew.put(ALLOW_REMOTE_ACCESS, ConfigParser.REMOTE_ACCESS);
        String ALLOW_REMOTE_PMS = "remote.pm";
        oldToNew.put(ALLOW_REMOTE_PMS, ConfigParser.ALLOW_REMOTE_PMS);
        String REMOTE_USERNAME = "remote.username";
        oldToNew.put(REMOTE_USERNAME, "versant.remoteUsername");
        String REMOTE_PASSWORD = "remote.password";
        oldToNew.put(REMOTE_PASSWORD, "versant.remotePassword");
        String RMI_REGISTRY_PORT = "rmi.registry.port";
        oldToNew.put(RMI_REGISTRY_PORT, ConfigParser.RMI_REGISTRY_PORT);
        String SERVER_PORT = "server.port";
        oldToNew.put(SERVER_PORT, ConfigParser.SERVER_PORT);
        String RMI_CLIENT_SF = "rmi.client.sf";
        oldToNew.put(RMI_CLIENT_SF, ConfigParser.RMI_CLIENT_SF);
        String RMI_CSF_IS_SSF = "rmi.csf.is.ssf";
        oldToNew.put(RMI_CSF_IS_SSF, ConfigParser.RMI_CSF_IS_SSF);
        String RMI_SERVER_SF = "rmi.server.sf";
        oldToNew.put(RMI_SERVER_SF, ConfigParser.RMI_SERVER_SF);
        String ALLOW_PM_CLOSE_WITH_OPEN_TX = "allow.pm.close.with.open.tx";
        oldToNew.put(ALLOW_PM_CLOSE_WITH_OPEN_TX,
                ConfigParser.ALLOW_PM_CLOSE_WITH_OPEN_TX);
        String PRECOMPILE_NAMED_QUERIES = "precompile.named.queries";
        oldToNew.put(PRECOMPILE_NAMED_QUERIES,
                ConfigParser.PRECOMPILE_NAMED_QUERIES);
        String CHECK_MODEL_CONSISTENCY_ON_COMMIT = "check.model.consistency.on.commit";
        oldToNew.put(CHECK_MODEL_CONSISTENCY_ON_COMMIT,
                ConfigParser.CHECK_MODEL_CONSISTENCY_ON_COMMIT);
        String INTERCEPT_DFG_FIELD_ACCESS = "intercept.dfg.field.access";
        oldToNew.put(INTERCEPT_DFG_FIELD_ACCESS,
                ConfigParser.INTERCEPT_DFG_FIELD_ACCESS);
        String PM_CACHE_REF_TYPE = "pm.cache.ref.type";
        oldToNew.put(PM_CACHE_REF_TYPE, ConfigParser.PM_CACHE_REF_TYPE);

        String HYPERDRIVE = "hyperdrive";
        oldToNew.put(HYPERDRIVE, ConfigParser.HYPERDRIVE);

        String PMPOOL_ENABLED = "pmpool.enabled";
        oldToNew.put(PMPOOL_ENABLED, ConfigParser.PMPOOL_ENABLED);
        String PMPOOL_MAX_IDLE = "pmpool.maxIdle";
        oldToNew.put(PMPOOL_MAX_IDLE, ConfigParser.PMPOOL_MAX_IDLE);

        String REMOTE_PMPOOL_ENABLED = "remote.pmpool.enabled";
        oldToNew.put(REMOTE_PMPOOL_ENABLED, ConfigParser.REMOTE_PMPOOL_ENABLED);
        String REMOTE_PMPOOL_MAX_IDLE = "remote.pmpool.maxIdle";
        oldToNew.put(REMOTE_PMPOOL_MAX_IDLE,
                ConfigParser.REMOTE_PMPOOL_MAX_IDLE);
        String REMOTE_PMPOOL_MAX_ACTIVE = "remote.pmpool.maxActive";
        oldToNew.put(REMOTE_PMPOOL_MAX_ACTIVE,
                ConfigParser.REMOTE_PMPOOL_MAX_ACTIVE);

        String FLUSH_THRESHOLD = "flush.threshold";
        oldToNew.put(FLUSH_THRESHOLD, ConfigParser.FLUSH_THRESHOLD);

        String STORE_COUNT = "storeCount";
        oldToNew.put(STORE_COUNT, null);
        String STORE = "store";
        oldToNew.put(STORE, null);
        String STORE_NAME = "store0.name";
        oldToNew.put(STORE_NAME, null);
        String STORE_TYPE = "store0.type";
        oldToNew.put(STORE_TYPE, ConfigParser.STORE_TYPE);
        String STORE_DB = "store0.db";
        oldToNew.put(STORE_DB, ConfigParser.STORE_DB);
        String STORE_URL = "store0.url";
        oldToNew.put(STORE_URL, ConfigParser.STD_CON_URL);
        String STORE_DRIVER = "store0.driver";
        oldToNew.put(STORE_DRIVER, ConfigParser.STD_CON_DRIVER_NAME);
        String STORE_USER = "store0.user";
        oldToNew.put(STORE_USER, ConfigParser.STD_CON_USER_NAME);
        String STORE_PASSWORD = "store0.password";
        oldToNew.put(STORE_PASSWORD, ConfigParser.STD_CON_PASSWORD);
        String STORE_PROPERTIES = "store0.properties";
        oldToNew.put(STORE_PROPERTIES, ConfigParser.STORE_PROPERTIES);
        String STORE_MAX_ACTIVE = "store0.maxActive";
        oldToNew.put(STORE_MAX_ACTIVE, ConfigParser.STORE_MAX_ACTIVE);
        String STORE_MAX_IDLE = "store0.maxIdle";
        oldToNew.put(STORE_MAX_IDLE, ConfigParser.STORE_MAX_IDLE);
        String STORE_MIN_IDLE = "store0.minIdle";
        oldToNew.put(STORE_MIN_IDLE, ConfigParser.STORE_MIN_IDLE);
        String STORE_RESERVED = "store0.reserved";
        oldToNew.put(STORE_RESERVED, ConfigParser.STORE_RESERVED);
        String STORE_TYPE_MAPPING_COUNT = "store0.jdbc.type.count";
        oldToNew.put(STORE_TYPE_MAPPING_COUNT, null);
        String STORE_JAVATYPE_MAPPING_COUNT = "store0.jdbc.javatype.count";
        oldToNew.put(STORE_JAVATYPE_MAPPING_COUNT, null);
        String STORE_NAMEGEN = "store0.jdbc.namegen";
        oldToNew.put(STORE_NAMEGEN, ConfigParser.STORE_NAMEGEN);
        String STORE_MIGRATION_CONTROLS = "store0.jdbc.migration";
        oldToNew.put(STORE_MIGRATION_CONTROLS,
                ConfigParser.STORE_MIGRATION_CONTROLS);
        String STORE_DISABLE_BATCHING = "store0.jdbc.nobatching";
        oldToNew.put(STORE_DISABLE_BATCHING,
                ConfigParser.STORE_DISABLE_BATCHING);
        String STORE_DISABLE_PS_CACHE = "store0.jdbc.disable.pscache";
        oldToNew.put(STORE_DISABLE_PS_CACHE,
                ConfigParser.STORE_DISABLE_PS_CACHE);
        String STORE_PS_CACHE_MAX = "store0.pscache.max";
        oldToNew.put(STORE_PS_CACHE_MAX, ConfigParser.STORE_PS_CACHE_MAX);
        String STORE_VALIDATE_SQL = "store0.validate.sql";
        oldToNew.put(STORE_VALIDATE_SQL, ConfigParser.STORE_VALIDATE_SQL);
        String STORE_INIT_SQL = "store0.init.sql";
        oldToNew.put(STORE_INIT_SQL, ConfigParser.STORE_INIT_SQL);
        String STORE_WAIT_FOR_CON_ON_STARTUP = "store0.wait.for.con.on.startup";
        oldToNew.put(STORE_WAIT_FOR_CON_ON_STARTUP,
                ConfigParser.STORE_WAIT_FOR_CON_ON_STARTUP);
        String STORE_TEST_ON_ALLOC = "store0.test.on.alloc";
        oldToNew.put(STORE_TEST_ON_ALLOC, ConfigParser.STORE_TEST_ON_ALLOC);
        String STORE_TEST_ON_RELEASE = "store0.test.on.release";
        oldToNew.put(STORE_TEST_ON_RELEASE, ConfigParser.STORE_TEST_ON_RELEASE);
        String STORE_TEST_ON_EXCEPTION = "store0.test.on.exception";
        oldToNew.put(STORE_TEST_ON_EXCEPTION,
                ConfigParser.STORE_TEST_ON_EXCEPTION);
        String STORE_TEST_WHEN_IDLE = "store0.test.when.idle";
        oldToNew.put(STORE_TEST_WHEN_IDLE, ConfigParser.STORE_TEST_WHEN_IDLE);
        String STORE_RETRY_INTERVAL_MS = "store0.retry.interval.ms";
        oldToNew.put(STORE_RETRY_INTERVAL_MS,
                ConfigParser.STORE_RETRY_INTERVAL_MS);
        String STORE_RETRY_COUNT = "store0.retry.count";
        oldToNew.put(STORE_RETRY_COUNT, ConfigParser.STORE_RETRY_COUNT);
        String STORE_VALIDATE_MAPPING_ON_STARTUP = "store0.validate.mapping.on.startup";
        oldToNew.put(STORE_VALIDATE_MAPPING_ON_STARTUP,
                ConfigParser.STORE_VALIDATE_MAPPING_ON_STARTUP);
        String STORE_CON_TIMEOUT = "store0.con.timeout";
        oldToNew.put(STORE_CON_TIMEOUT, ConfigParser.STORE_CON_TIMEOUT);
        String STORE_TEST_INTERVAL = "store0.test.interval";
        oldToNew.put(STORE_TEST_INTERVAL, ConfigParser.STORE_TEST_INTERVAL);
        String STORE_ISOLATION_LEVEL = "store0.isolation.level";
        oldToNew.put(STORE_ISOLATION_LEVEL, ConfigParser.STORE_ISOLATION_LEVEL);
        String STORE_BLOCK_WHEN_FULL = "store0.block.when.full";
        oldToNew.put(STORE_BLOCK_WHEN_FULL, ConfigParser.STORE_BLOCK_WHEN_FULL);
        String STORE_MAX_CON_AGE = "store0.max.con.age";
        oldToNew.put(STORE_MAX_CON_AGE, ConfigParser.STORE_MAX_CON_AGE);
        String STORE_MANAGED_ONE_TO_MANY = "store0.managed.one.to.many";
        oldToNew.put(STORE_MANAGED_ONE_TO_MANY,
                ConfigParser.STORE_MANAGED_ONE_TO_MANY);
        String STORE_MANAGED_MANY_TO_MANY = "store0.managed.many.to.many";
        oldToNew.put(STORE_MANAGED_MANY_TO_MANY,
                ConfigParser.STORE_MANAGED_MANY_TO_MANY);
        String STORE_SCO_FACTORY_COUNT = "store0.sco.factory.count";
        oldToNew.put(STORE_SCO_FACTORY_COUNT, null);
        String STORE_OID_BATCH_SIZE = "store0.OID.batch.size";
        oldToNew.put(STORE_OID_BATCH_SIZE, ConfigParser.VDS_OID_BATCH_SIZE);
        String STORE_SCHEMA_DEFINITION = "store0.schema.define";
        oldToNew.put(STORE_SCHEMA_DEFINITION,
                ConfigParser.VDS_SCHEMA_DEFINITION);
        String STORE_SCHEMA_EVOLUTION = "store0.schema.evolve";
        oldToNew.put(STORE_SCHEMA_EVOLUTION, ConfigParser.VDS_SCHEMA_EVOLUTION);

        String MDEDIT_SRC_PATH = "mdedit.srcPath";
        oldToNew.put(MDEDIT_SRC_PATH, ConfigParser.MDEDIT_SRC_PATH);
        String MDEDIT_CP_COUNT = "mdedit.classPathCount";
        oldToNew.put(MDEDIT_CP_COUNT, null);

        String JDO_FILE_COUNT = "jdoFileCount";
        oldToNew.put(JDO_FILE_COUNT, null);

        String EVENT_LOGGING = "event.logging";
        oldToNew.put(EVENT_LOGGING, ConfigParser.EVENT_LOGGING);

        String DATASTORE_TX_LOCKING = "datastore.tx.locking";
        oldToNew.put(DATASTORE_TX_LOCKING, ConfigParser.DATASTORE_TX_LOCKING);

        String CACHE_ENABLED = "cache.enabled";
        oldToNew.put(CACHE_ENABLED, ConfigParser.CACHE_ENABLED);
        String CACHE_MAX_OBJECTS = "cache.maxobjects";
        oldToNew.put(CACHE_MAX_OBJECTS, ConfigParser.CACHE_MAX_OBJECTS);
        String CACHE_LISTENER = "cache.listener";
        oldToNew.put(CACHE_LISTENER, ConfigParser.CACHE_LISTENER);
        String CACHE_CLUSTER_TRANSPORT = "cache.cluster.transport";
        oldToNew.put(CACHE_CLUSTER_TRANSPORT,
                ConfigParser.CACHE_CLUSTER_TRANSPORT);

        String QUERY_CACHE_ENABLED = "query.cache.enabled";
        oldToNew.put(QUERY_CACHE_ENABLED, ConfigParser.QUERY_CACHE_ENABLED);
        String QUERY_CACHE_MAX_QUERIES = "query.cache.max.queries";
        oldToNew.put(QUERY_CACHE_MAX_QUERIES,
                ConfigParser.QUERY_CACHE_MAX_QUERIES);

        String ANT_DISABLED = "ant.disabled";
        oldToNew.put(ANT_DISABLED, ConfigParser.ANT_DISABLED);
        String ANT_BUILDFILE = "ant.buildfile";
        oldToNew.put(ANT_BUILDFILE, ConfigParser.ANT_BUILDFILE);
        String ANT_RUN_TARGET = "ant.run.target";
        oldToNew.put(ANT_RUN_TARGET, ConfigParser.ANT_RUN_TARGET);
        String ANT_COMPILE = "ant.compile";
        oldToNew.put(ANT_COMPILE, ConfigParser.ANT_COMPILE);
        String ANT_ARGS = "ant.args";
        oldToNew.put(ANT_ARGS, ConfigParser.ANT_ARGS);
        String ANT_SHOW_ALL_TARGETS = "ant.show.all.targets";
        oldToNew.put(ANT_SHOW_ALL_TARGETS, ConfigParser.ANT_SHOW_ALL_TARGETS);

        String SCRIPT_DIR = "script.dir";
        oldToNew.put(SCRIPT_DIR, ConfigParser.SCRIPT_DIR);

        String METRIC_SNAPSHOT_INTERVAL_MS = "metric.snapshot.interval.ms";
        oldToNew.put(METRIC_SNAPSHOT_INTERVAL_MS,
                ConfigParser.METRIC_SNAPSHOT_INTERVAL_MS);
        String METRIC_STORE_CAPACITY = "metric.store.capacity";
        oldToNew.put(METRIC_STORE_CAPACITY, ConfigParser.METRIC_STORE_CAPACITY);

        String LOG_DOWNLOADER = "log.downloader";
        oldToNew.put(LOG_DOWNLOADER, ConfigParser.LOG_DOWNLOADER);

        String EXTERNALIZER_COUNT = "externalizer.count";
        oldToNew.put(EXTERNALIZER_COUNT, null);

        String TESTING = "testing";
        oldToNew.put(TESTING, ConfigParser.TESTING);

    }

    
    public static void main(String[] args) {
        String src = null;
        String dest = null;
        for (int i = 0; i < args.length; i++) {
            if (i == 0) src = args[i];
            if (i == 1) dest = args[i];
        }
        if (src == null) {
            printHelp();
        }
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            System.out.println("Input file " + src + " does not exist.");
            printHelp();
            System.exit(-1);
        }
        File destFile = null;
        if (dest == null) {
            int last = src.lastIndexOf('.');
            dest = src.substring(0, last) + ".properties";
            destFile = new File(dest);
            if (destFile.exists()) {
                System.out.println(
                        "File " + dest + " will be created by this tool,");
                System.out.println(
                        "but it already exists. (we do not want to overide existing files) ");
                System.exit(-1);

            }
        }
        if (dest != null) {
            destFile = new File(dest);
            if (!destFile.exists()) {
                try {
                    destFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("Could not create file " + dest);
                    System.exit(-1);
                }
            }
        }
        Properties p = new Properties();
        try {
            InputStream in = null;
            try {
                in = new FileInputStream(src);

                try {
                    p.load(in);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                if (in != null) in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList lines = new ArrayList();
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(srcFile));
                String[] newLines = null;
                // Read file
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    newLines = getConvertedLine(line);
                    for (int i = 0; i < newLines.length; i++) {
                        lines.add(newLines[i]);
                    }
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
            try {
                writer = new BufferedWriter(new FileWriter(destFile, false));
                // Write file
                Iterator iter = lines.iterator();
                while (iter.hasNext()) {
                    writer.write((String)iter.next());
                    writer.newLine();
                }
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    //hide
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private static String[] getConvertedLine(String line) {
        if (line.trim().equals("")) {
            return new String[]{line};
        } else if (line.trim().startsWith("#")) {
            return new String[]{line};
        } else {
            return getPropLines(line);
        }
    }

    private static String[] getPropLines(String line) {
        int first = line.indexOf('=');
        Properties p = new Properties();
        p.setProperty(line.substring(0, first),
                line.substring(first + 1, line.length()));
        convert(p, true);
        Set set = p.keySet();
        String[] props = new String[set.size()];
        StringBuffer newLine = null;
        int i = 0;
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            String s = (String)iter.next();
            newLine = new StringBuffer();
            newLine.append(s);
            newLine.append("=");
            newLine.append(p.getProperty(s));
            props[i++] = newLine.toString();
        }
        return props;
    }

    private static void printHelp() {
        System.out.println(
                "usage: com.versant.core.common.config.PropertyConverter <inputFile> [<outputFile>]");
        System.out.println(
                "  <inputFile>    The old *.jdogenie file to be converted.");
        System.out.println(
                "                 If output file is not specified then the output");
        System.out.println(
                "                 file will be <inputFile>.properties");
        System.out.println("  <outputFile>   The new file .properties file.");
        System.exit(-1);
    }

    /**
     * Converts the old jdogenie properties to the new versant properties.
     *
     * @param original
     * @return the new versant properties
     */
    public static boolean convert(Properties original) {
        return convert(original, false);
    }

    /**
     * Converts the old jdogenie properties to the new versant properties.
     *
     * @param original
     * @return the new versant properties
     */
    private static boolean convert(Properties original, boolean converted) {
        Properties oldProps = (Properties)original.clone();
        Properties newProps = new Properties();
        ArrayList list = new ArrayList(oldProps.keySet());
        Collections.sort(list);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            String key = (String)iter.next();
            if (oldToNew.containsKey(key)) {
                if (oldToNew.get(key) != null) {
                    newProps.setProperty((String)oldToNew.get(key),
                            oldProps.getProperty(key));
                }
                // ignore prop if oldToNew is null
                converted = true;
            } else {
                String STORE_EXT = "store0.ext.";
                String STORE_TYPE_MAPPING = "store0.jdbc.type.";
                String STORE_JAVATYPE_MAPPING = "store0.jdbc.javatype.";
                String STORE_SCO_FACTORY_MAPPING = "store0.sco.factory.mapping";
                String JDO = "jdo";
                String METRIC_USER = "metric.user.";
                String EXTERNALIZER = "externalizer.";
                String DIAGRAM = "diagram";
                String MDEDIT_CP = "mdedit.cp";
                if (key.startsWith(STORE_EXT)) {
                    String newKey = ConfigParser.STORE_EXT +
                            key.substring(STORE_EXT.length(), key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(STORE_TYPE_MAPPING)) {
                    String newKey = ConfigParser.STORE_TYPE_MAPPING +
                            key.substring(STORE_TYPE_MAPPING.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(STORE_JAVATYPE_MAPPING)) {
                    String newKey = ConfigParser.STORE_JAVATYPE_MAPPING +
                            key.substring(STORE_JAVATYPE_MAPPING.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(STORE_SCO_FACTORY_MAPPING)) {
                    String newKey = ConfigParser.STORE_SCO_FACTORY_MAPPING +
                            key.substring(STORE_SCO_FACTORY_MAPPING.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                } else if (key.startsWith(JDO)) {
                    String newKey = ConfigParser.JDO +
                            key.substring(JDO.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(METRIC_USER)) {
                    String newKey = ConfigParser.METRIC_USER +
                            key.substring(METRIC_USER.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(EXTERNALIZER)) {
                    String newKey = ConfigParser.EXTERNALIZER +
                            key.substring(EXTERNALIZER.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(DIAGRAM)) {
                    String newKey = ConfigParser.DIAGRAM +
                            key.substring(DIAGRAM.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else if (key.startsWith(MDEDIT_CP)) {
                    String newKey = ConfigParser.MDEDIT_CP +
                            key.substring(MDEDIT_CP.length(),
                                    key.length());
                    newProps.setProperty(newKey, oldProps.getProperty(key));
                    converted = true;
                } else {

                    newProps.put(key, oldProps.get(key));
                    //newProps.setProperty(key, oldProps.getProperty(key));
                }
            }
        }
        if (converted) {
            fillAsProps(newProps, ConfigParser.EVENT_LOGGING);  //
            fillAsProps(newProps, ConfigParser.RMI_CLIENT_SF);  //
            fillAsProps(newProps, ConfigParser.RMI_SERVER_SF);  //
            fillAsProps(newProps, ConfigParser.CACHE_CLUSTER_TRANSPORT); //
            fillAsProps(newProps, ConfigParser.CACHE_LISTENER);
            fillAsProps(newProps, ConfigParser.STORE_NAMEGEN);   //
            fillAsProps(newProps, ConfigParser.STORE_MIGRATION_CONTROLS); //
            fillAsProps(newProps,
                    ConfigParser.STORE_EXT +
                    JdoExtension.toKeyString(
                            JdoExtensionKeys.JDBC_KEY_GENERATOR));   //
            fillAsProps(newProps, ConfigParser.LOG_DOWNLOADER);  //
            fillExternalizer(newProps);
        }

        original.clear();
        original.putAll(newProps);
        if ("za.co.hemtech.jdo.client.BootstrapPMF".equals(original.getProperty(
                ConfigParser.PMF_CLASS))) {
            original.setProperty(ConfigParser.PMF_CLASS,
                    "com.versant.core.jdo.BootstrapPMF");
        }
        return converted;
    }

    /**
     * Fill all the given properties into newProps.
     *
     * @param newProps
     */
    private static void fillAsProps(Properties newProps, String lookup) {
        String s = newProps.getProperty(lookup);
        if (s != null) {
            HashMap logging = new HashMap(17);
            StringListParser lp = new StringListParser(s);
            String name = null; // skip unused class name
            try {
                name = lp.nextQuotedString(); // skip unused class name
            } catch (IllegalStateException e) {
                return;
            }
            lp.nextProperties(logging);
            Set names = logging.keySet();
            for (Iterator iter = names.iterator(); iter.hasNext();) {
                String key = (String)iter.next();
                newProps.setProperty(lookup + "." + key,
                        (String)logging.get(key));
            }
            newProps.remove(lookup);
            if (name != null) {
                newProps.setProperty(lookup, name);
            }
        }
    }

    /**
     * Fill all the externalizer properties into newProps.
     *
     * @param newProps
     */
    private static void fillExternalizer(Properties newProps) {
        String s;
        for (int i = 0; i < ConfigParser.MAX_EXTERNALIZER_COUNT; i++) {
            String key = ConfigParser.EXTERNALIZER + i;
            s = newProps.getProperty(key);
            if (s != null) {
                StringListParser lp = new StringListParser(s);
                Object o = newProps.setProperty(key + ConfigParser.EXTERNALIZER_TYPE,
                        lp.nextString());
                if (o != null) {
                    newProps.setProperty(key + ConfigParser.EXTERNALIZER_TYPE,
                            (String)o);
                    continue;
                }
                boolean enabled = lp.nextBoolean();
                newProps.setProperty(key + ConfigParser.EXTERNALIZER_ENABLED,
                        (enabled ? "true" : "false"));
                String externalizerName = lp.nextQuotedString();
                if (externalizerName != null) {
                    newProps.setProperty(key + ConfigParser.EXTERNALIZER_CLASS,
                            externalizerName);
                }

                HashMap typeKeys = new HashMap(17);
                lp.nextProperties(typeKeys);
                Set names = typeKeys.keySet();
                for (Iterator iter = names.iterator(); iter.hasNext();) {
                    String typeKey = (String)iter.next();
                    newProps.setProperty(key +
                            ConfigParser.EXTERNALIZER_CLASS + "." + typeKey,
                            (String)typeKeys.get(typeKey));
                }
                newProps.remove(key);
            }
        }
    }

}

