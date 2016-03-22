
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
package com.versant.core.jdo.tools.workbench;

import za.co.hemtech.config.Config;
import za.co.hemtech.config.DelimList;
import za.co.hemtech.config.DelimListParser;
import za.co.hemtech.gui.model.ObservableList;
import com.versant.core.jdo.tools.workbench.model.ClassDiagram;
import com.versant.core.jdo.tools.workbench.model.*;
import com.versant.core.util.StringListParser;
import com.versant.core.util.StringList;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * Global settings for the Workbench. These are stored in .openaccess.ini
 * in the users home directory.
 */
public class WorkbenchSettings {

    public static final Config USER_CFG = Config.getInstance("Global");
    private static final WorkbenchSettings instance = new WorkbenchSettings();

    public String antCommand = "ant";

    private List recentProjectList = new ArrayList();   // of File
    private File lastProjectDir;
    private List driverList = new ArrayList();              // of MdDriver
    private List remoteServerList = new ObservableList();   // of MdRemoteServer
    private List dateFormatList = new ArrayList();          // of String
    private int lastUsedDateFormat;
    private ClassDiagram.Settings diagramSettings = new ClassDiagram.Settings();
    private Rectangle windowState;
    public static final int MAX_RECENT_PROJECTS = 20;

    public static final String THEME_KEY = "theme";

    public static final int PERF_LAYOUT_TYPE_DEFAULT = 1;
    public static final int PERF_LAYOUT_TYPE_CLUSTER = 2;

    private static final Color TREE_LINE_COLOR = new Color(235, 235, 235);

    private WorkbenchSettings() {
    }

    public static WorkbenchSettings getInstance() {
        return instance;
    }

    /**
     * Load settings.
     */
    public void load() throws Exception {
        // recent projects
        Config c = USER_CFG.getConfig("RecentProjects");
        int n = c.getInt("size");
        recentProjectList.clear();
        if (n == 0) {
            recentProjectList.add(new File("demos/tut1/versant.properties"));
            recentProjectList.add(new File("demos/tut1sb-jca/versant.properties"));
            recentProjectList.add(new File("demos/legacydb/versant.properties"));
            recentProjectList.add(new File("demos/testcenter/versant.properties"));
            recentProjectList.add(new File("demos/junit/versant.properties"));
            recentProjectList.add(new File("demos/parentchild/versant.properties"));
            recentProjectList.add(new File("demos/interfaces/versant.properties"));
            recentProjectList.add(new File("demos/college/versant.properties"));
            recentProjectList.add(new File("demos/externalization/versant.properties"));
            recentProjectList.add(new File("demos/customtypes/versant.properties"));
            recentProjectList.add(new File("demos/xmlbinding-jibx/versant.properties"));
            recentProjectList.add(new File("demos/graph/versant.properties"));
            recentProjectList.add(new File("demos/aggregates/versant.properties"));
            recentProjectList.add(new File("demos/attachdetach/versant.properties"));
            recentProjectList.add(new File("demos/inheritance/versant.properties"));
            recentProjectList.add(new File("demos/batchprocess/versant.properties"));
            recentProjectList.add(new File("demos/enums/versant.properties"));
            recentProjectList.add(new File("demos/embedded/versant.properties"));
        } else {
            for (int i = 0; i < n; i++) {
                recentProjectList.add(
                        new File(c.getString(Integer.toString(i))));
            }
        }
        String s = c.getString("lastDir");
        if (s != null) {
            lastProjectDir = new File(s);
        } else {
            lastProjectDir = null;
        }

        initDrivers();

        // remote servers
        c = USER_CFG.getConfig("RemoteServers");
        n = c.getInt("size");
        remoteServerList.clear();
        for (int i = 0; i < n; i++) {
            MdRemoteServer rs = new MdRemoteServer();
            StringListParser p = new StringListParser(
                    c.getString(Integer.toString(i)));
            rs.load(p);
            remoteServerList.add(rs);
        }

        // date formats
        c = USER_CFG.getConfig("DateFormats");
        n = c.getInt("size");
        dateFormatList.clear();
        if (n == 0) {
            String[] a = new String[]{
                "yyyy.MM.dd", "dd.MM.yyyy", "yyyy/MM/dd hh:mm:ss",
                "yyyy/MM/dd hh:mm:ss:SSS"};
            for (int i = 0; i < a.length; i++) dateFormatList.add(a[i]);
        } else {
            for (int i = 0; i < n; i++) {
                dateFormatList.add(c.getString(Integer.toString(i)));
            }
        }
        lastUsedDateFormat = c.getInt("lastUsed");
        if (lastUsedDateFormat >= dateFormatList.size()) lastUsedDateFormat = 0;

        // diagram settings
        c = USER_CFG.getConfig("DiagramSettings");
        Properties p = new Properties();
        addKeyProps(p, ClassDiagram.Settings.KEY_GENERAL, c);
        addKeyProps(p, ClassDiagram.Settings.KEY_CLASS, c);
        addKeyProps(p, ClassDiagram.Settings.KEY_FIELD, c);
        diagramSettings.load(p);
    }

    private void addKeyProps(Properties p, String key, Config c) {
        String value = c.getString(key);
        if (value != null) {
            p.setProperty(key, value);
        }
    }

    private String getDefString(Config c, String key, String def) {
        String s = c.getString(key);
        return s == null ? def : s;
    }

    private boolean getCfgBoolean(Config c, String name, boolean def) {
        String s = c.getString(name);
        return s == null ? def : s.equals("Y");
    }

    private void initDrivers() {
        String[] def = new String[]{
            "com.intersys.jdbc.CacheDriver",
            "jdbc:Cache://host:port/database_name",
            "cache",
            null,
            "com.ibm.db2.jcc.DB2Driver",
            "jdbc:db2://host:port/database_name",
            "db2",
            "driverType=4",
            "org.firebirdsql.jdbc.FBDriver",
            "jdbc:firebirdsql://host/path/to/database.gdb",
            "firebird",
            null,
            "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:hsql://host/database_name",
            "hypersonic",
            null,
            "com.informix.jdbc.IfxDriver",
            "jdbc:informix-sqli:host:port/database_name",
            "informix",
            "INFORMIXSERVER=ifmxserver_tcp",
            "com.informix.jdbc.IfxDriver",
            "jdbc:informix-sqli:host:port/database_path",
            "informixse",
            "INFORMIXSERVER=server_name",
            "interbase.interclient.Driver",
            "jdbc:interbase://host/path/to/database.gdb",
            "interbase",
            null,
            "com.microsoft.jdbc.sqlserver.SQLServerDriver",
            "jdbc:microsoft:sqlserver://host:port",
            "mssql",
            "DatabaseName=database_name;SelectMethod=cursor",
            "com.mysql.jdbc.Driver",
            "jdbc:mysql://host:port/database_name",
            "mysql",
            null,
            "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:thin:@host:port:database_name",
            "oracle",
            null,
            "com.pointbase.jdbc.jdbcUniversalDriver",
            "jdbc:pointbase:server://host/database_name",
            "pointbase",
            null,
            "org.postgresql.Driver",
            "jdbc:postgresql://host/database_name",
            "postgres",
            null,
            "com.sap.dbtech.jdbc.DriverSapDB",
            "jdbc:sapdb://host/database_name",
            "sapdb",
            null,
            "com.sybase.jdbc2.jdbc.SybDriver",
            "jdbc:sybase:Tds:host:port/database_name",
            "sybase",
            null,
            "[no driver required]",
            "versant:database[@hostname][:portno]",
            "versant",
            null,
        };
        int n = def.length;
        for (int i = 0; i < n;) {
            String driverClass = def[i++];
            String sampleURL = def[i++];
            String database = def[i++];
            String properties = def[i++];
            driverList.add(
                    new MdDriver(driverClass, sampleURL, database, properties));
        }
    }

    /**
     * Save the current settings.
     */
    public void save() throws Exception {
        String version = MdUtils.getVersion();
        if (version != null) USER_CFG.setString("version", version);

        DelimList l = new DelimList();
        if (windowState != null) {
            l.append(windowState.x);
            l.append(windowState.y);
            l.append(windowState.width);
            l.append(windowState.height);
            USER_CFG.setString("mainBounds", l.toString());
        }

        l.reset();
//        l.append(mainFrame.getSplitter().getDividerLocation());
//        USER_CFG.setString("splitter", l.toString());

        // recent projects
        Config c = USER_CFG.getConfig("RecentProjects");
        int n = recentProjectList.size();
        c.setInt("size", n);
        for (int i = 0; i < n; i++) {
            c.setString(Integer.toString(i),
                    recentProjectList.get(i).toString());
        }
        if (lastProjectDir != null) {
            c.setString("lastDir", lastProjectDir.toString());
        }

        // remote servers
        c = USER_CFG.getConfig("RemoteServers");
        n = remoteServerList.size();
        c.setInt("size", n);
        for (int i = 0; i < n; i++) {
            MdRemoteServer rs = (MdRemoteServer)remoteServerList.get(i);
            StringList sl = new StringList();
            rs.save(sl);
            c.setString(Integer.toString(i), sl.toString());
        }
        for (int i = n; i < 100; i++) c.remove(Integer.toString(i));

        // date formats
        c = USER_CFG.getConfig("DateFormats");
        n = dateFormatList.size();
        c.setInt("size", n);
        for (int i = 0; i < n; i++) {
            c.setString(Integer.toString(i), (String)dateFormatList.get(i));
        }
        c.setInt("lastUsed", lastUsedDateFormat);

        // diagram settings
        c = USER_CFG.getConfig("DiagramSettings");
        Properties p = new Properties();
        diagramSettings.save(p);
        for (Iterator it = p.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            c.setString(key, value);
        }
        USER_CFG.save();
    }

    public void restoreFrameState() {
        String s = USER_CFG.getString("mainBounds");
        DelimListParser p = new DelimListParser();
        if (s == null) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            windowState = new Rectangle(10, 10, d.width - 20, d.height - 74);
        } else {
            p.setString(s);
            windowState = new Rectangle(p.nextInt(), p.nextInt(),
                    p.nextInt(), p.nextInt());
        }

//        s = USER_CFG.getString("splitter");
//        if (s == null) {
//            mainFrame.getSplitter().setDividerLocation(mainFrame.getHeight() * 4 / 5);
//        } else {
//            p.setString(s);
//            mainFrame.getSplitter().setDividerLocation(p.nextInt());
//        }
    }

    public ClassDiagram.Settings getDiagramSettings() {
        return diagramSettings;
    }

    public List getRecentProjectList() {
        return recentProjectList;
    }

    public void addRecentProject(File f) {
        recentProjectList.remove(f);
        recentProjectList.add(0, f);
        int n = recentProjectList.size();
        if (n >= MAX_RECENT_PROJECTS) recentProjectList.remove(n - 1);
    }

    public void removeRecentProject(File f) {
        recentProjectList.remove(f);
    }

    public File getLastProjectDir() {
        if(lastProjectDir == null){
            File lastProject = getLastProject();
            if(lastProject != null){
                return lastProject.getParentFile();
            }
        }
        return lastProjectDir;
    }

    public void setLastProjectDir(File lastProjectDir) {
        this.lastProjectDir = lastProjectDir;
    }

    public boolean getReopenLastProject() {
        return getCfgBoolean(USER_CFG, "reopenLastProject", true);
    }

    public void setReopenLastProject(boolean reopenLastProject) {
        USER_CFG.setBoolean("reopenLastProject", reopenLastProject);
    }

    public File getLastProject() {
        if (recentProjectList.isEmpty()) return null;
        return (File)recentProjectList.get(0);
    }

    public boolean getShowAboutOnStart() {
        return getCfgBoolean(USER_CFG, "showAboutOnStart", true);
    }

    public void setShowAboutOnStart(boolean showAboutOnStart) {
        USER_CFG.setBoolean("showAboutOnStart", showAboutOnStart);
    }

    public boolean getRunDatabaseParseOnStart() {
        return getCfgBoolean(USER_CFG, "runDatabaseParseOnStart", true);
    }

    public void setRunDatabaseParseOnStart(boolean runDatabaseParseOnStart) {
        USER_CFG.setBoolean("runDatabaseParseOnStart", runDatabaseParseOnStart);
    }

    public boolean getDatabaseToLowerCase() {
        return getCfgBoolean(USER_CFG, "databaseToLowerCase", false);
    }

    public void setDatabaseToLowerCase(boolean databaseToLowerCase) {
        USER_CFG.setBoolean("databaseToLowerCase", databaseToLowerCase);
    }

    public boolean getShowOpenCreateOnStart() {
        return getCfgBoolean(USER_CFG, "showOpenCreateOnStart", true);
    }

    public void setShowOpenCreateOnStart(boolean showAboutOnStart) {
        USER_CFG.setBoolean("showOpenCreateOnStart", showAboutOnStart);
    }

    public boolean getCheckAntOnStartup() {
        return getCfgBoolean(USER_CFG, "checkAntOnStartup", true);
    }

    public void setCheckAntOnStartup(boolean on) {
        USER_CFG.setBoolean("checkAntOnStartup", on);
    }

    public boolean getShowNoClassesWarning() {
        return getCfgBoolean(USER_CFG, "showNoClassesWarning", true);
    }

    public void setShowNoClassesWarning(boolean on) {
        USER_CFG.setBoolean("showNoClassesWarning", on);
    }

    public boolean getShowNotEnhancedWarning() {
        return getCfgBoolean(USER_CFG, "showNotEnhancedWarning", true);
    }

    public void setShowExplainLocking(boolean on) {
        USER_CFG.setBoolean("showExplainLocking", on);
    }

    public boolean getShowExplainLocking() {
        return getCfgBoolean(USER_CFG, "showExplainLocking", true);
    }

    public void setShowNotEnhancedWarning(boolean on) {
        USER_CFG.setBoolean("showNotEnhancedWarning", on);
    }

    public boolean getShowCompileFailedWarning() {
        return getCfgBoolean(USER_CFG, "showCompileFailedWarning", true);
    }

    public void setShowCompileFailedWarning(boolean on) {
        USER_CFG.setBoolean("showCompileFailedWarning", on);
    }

    public boolean getReloadClassesOnActivate() {
        return getCfgBoolean(USER_CFG, "reloadClassesOnActivate", true);
    }

    public void setReloadClassesOnActivate(boolean on) {
        USER_CFG.setBoolean("reloadClassesOnActivate", on);
    }

    public boolean getShowAllFields() {
        return getCfgBoolean(USER_CFG, "showNoClassesWarning", true);
    }

    public boolean getOpenNewTabJdo() {
        return getCfgBoolean(USER_CFG, "openNewTabJdo", true);
    }

    public void setOpenNewTabJdo(boolean on) {
        USER_CFG.setBoolean("openNewTabJdo", on);
    }

    public boolean getOpenNewTabSql() {
        return getCfgBoolean(USER_CFG, "openNewTabSql", true);
    }

    public void setOpenNewTabSql(boolean on) {
        USER_CFG.setBoolean("openNewTabSql", on);
    }

    public String getWinBrowser() {
        return getDefString(USER_CFG, "winBrowser", BrowserControl.WIN_CMD);
    }

    public void setWinBrowser(String winBrowser) {
        USER_CFG.setString("winBrowser", winBrowser);
    }

    public String getUnixBrowser() {
        return getDefString(USER_CFG, "unixBrowser", BrowserControl.UNIX_CMD);
    }

    public void setUnixBrowser(String unixBrowser) {
        USER_CFG.setString("unixBrowser", unixBrowser);
    }

    public String getManualPath() {
        return getDefString(USER_CFG, "manualPath", "docs/index.html");
    }

    public void setManualPath(String manualPath) {
        USER_CFG.setString("manualPath", manualPath);
    }

    public List getDriverList() {
        return driverList;
    }

    public Config getInternalFrameConfig() {
        return USER_CFG.getConfig("InternalFrame");
    }

    public Config getDesktopConfig() {
        return USER_CFG.getConfig("Desktop");
    }

    public Config getDialogConfig() {
        return USER_CFG.getConfig("Dialogs");
    }

    public File getTokenPropertiesFile(File f) {
        if (f == null) return null;
        Config c = getTokenPropertyFilesConfig();
        String s = c.getString(f.toString());
        if (s == null) return null;
        return new File(s);
    }

    public void setTokenPropertiesFile(File f, File p) {
        if (f == null) return;
        Config c = getTokenPropertyFilesConfig();
        if (p == null) {
            c.remove(f.toString());
        } else {
            c.setString(f.toString(), p.toString());
        }
    }

    private Config getTokenPropertyFilesConfig() {
        return USER_CFG.getConfig("TokenPropertyFiles");
    }

    public List getRemoteServerList() {
        return remoteServerList;
    }

    public void addRemoteServer(MdRemoteServer rs) {
        remoteServerList.add(rs);
    }

    public void removeRemoteServer(MdRemoteServer rs) {
        remoteServerList.remove(rs);
    }

    public List getDateFormatList() {
        return dateFormatList;
    }

    public int getLastUsedDateFormat() {
        return lastUsedDateFormat;
    }

    public DateFormat getDateFormat() {
        WorkbenchSettings settings = WorkbenchSettings.getInstance();
        List l = settings.getDateFormatList();
        String s = (String)l.get(settings.getLastUsedDateFormat());
        return new SimpleDateFormat(s);
    }

    public void setLastUsedDateFormat(int lastUsedDateFormat) {
        this.lastUsedDateFormat = lastUsedDateFormat;
    }

    public String getLastUsedDateFormatString() {
        return (String)dateFormatList.get(lastUsedDateFormat);
    }

    public String getAntCommand() {

        String s = getDefString(USER_CFG, "antCommand", antCommand);
        if (s.length() == 0) s = antCommand;
        return s;
    }

    public void setAntCommand(String ac) {
        USER_CFG.setString("antCommand", ac);
    }

    public boolean getCloseAntDialogOnSuccess() {
        return getCfgBoolean(USER_CFG, "closeAntDialogOnSuccess", false);
    }

    public void setCloseAntDialogOnSuccess(boolean on) {
        USER_CFG.setBoolean("closeAntDialogOnSuccess", on);
    }

    public String getTheme() {
        return getDefString(USER_CFG, THEME_KEY, "default");
    }

    public void setTheme(String ac) {
        USER_CFG.setString(THEME_KEY, ac);
    }

    public boolean getSqlCommentsInScripts() {
        return getCfgBoolean(USER_CFG, "sqlCommentsInScripts", true);
    }

    public void setSqlCommentsInScripts(boolean on) {
        USER_CFG.setBoolean("sqlCommentsInScripts", on);
    }

    public int getChooseClassView() {
        return USER_CFG.getInt("chooseClassView");
    }

    public void setChooseClassView(int v) {
        USER_CFG.setInt("chooseClassView", v);
    }

    public int getLastTreeView() {
        return USER_CFG.getInt("lastTreeView");
    }

    public void setLastTreeView(int v) {
        USER_CFG.setInt("lastTreeView", v);
    }

    public boolean isSlowDisplay() {
        return getCfgBoolean(USER_CFG, "slowDisplay", false);
    }

    public void setSlowDisplay(boolean on) {
        USER_CFG.setBoolean("slowDisplay", on);
    }

    public boolean isPromptHsqldb() {
        return getCfgBoolean(USER_CFG, "promptHsqldb", true);
    }

    public void setPromptHsqldb(boolean on) {
        USER_CFG.setBoolean("promptHsqldb", on);
    }

    public boolean isPromptRun() {
        return getCfgBoolean(USER_CFG, "promptRun", true);
    }

    public void setPromptRun(boolean on) {
        USER_CFG.setBoolean("promptRun", on);
    }

    /**
     * Get the color to use no commbined tree and table views.
     */
    public Color getTreeTableGridLineColor() {
        return TREE_LINE_COLOR;
        // do not use alpha blended line - very slow
        //return isSlowDisplay() ? TREE_LINE_COLOR : TREE_LINE_COLOR_ALPHA;
    }

    /**
     * Get the config for storing info about the management console.
     */
    public Config getConsoleConfig() {
        return USER_CFG.getConfig("ManagementConsole");
    }

    public boolean isMetricsExportVisible() {
        return getCfgBoolean(USER_CFG, "metricsExportVisible", true);
    }

    public void setMetricsExportVisible(boolean on) {
        USER_CFG.setBoolean("metricsExportVisible", on);
    }

    public boolean isMetricsExportSelected() {
        return getCfgBoolean(USER_CFG, "metricsExportSelected", true);
    }

    public void setMetricsExportSelected(boolean on) {
        USER_CFG.setBoolean("metricsExportSelected", on);
    }

    public boolean isMetricsExportMs() {
        return getCfgBoolean(USER_CFG, "metricsExportMs", false);
    }

    public void setMetricsExportMs(boolean on) {
        USER_CFG.setBoolean("metricsExportMs", on);
    }

    public boolean isMetricsExportDate() {
        return getCfgBoolean(USER_CFG, "metricsExportDate", true);
    }

    public void setMetricsExportDate(boolean on) {
        USER_CFG.setBoolean("metricsExportDate", on);
    }

    private String createPerfLayoutKey(int type) {
        switch (type) {
            case PERF_LAYOUT_TYPE_CLUSTER:
                return "perfLayout.cluster";
        }
        return "perfLayout.default";
    }

    public String getPerfLayout(int type) {
        String s = USER_CFG.getString(createPerfLayoutKey(type));
        if (s == null) {
            switch (type) {
                case PERF_LAYOUT_TYPE_CLUSTER:
                    s = "cluster.jdoperflayout";
                    break;
                default:
                    s = "default.jdoperflayout";
            }
        }
        return s;
    }

    public void setPerfLayout(int type, String s) {
        String key = createPerfLayoutKey(type);
        if (s == null) {
            USER_CFG.remove(key);
        } else {
            USER_CFG.setString(key, s);
        }
    }

    public String getJDOGenieHome() {
        return System.getProperty("OPENACCESS_HOME", "$OPENACCESS_HOME/");
    }

    public boolean isDisableHyperdriveInWorkbench() {
        return getCfgBoolean(USER_CFG, "disableHyperdriveInWorkbench", true);
    }

    public void setDisableHyperdriveInWorkbench(boolean on) {
        USER_CFG.setBoolean("disableHyperdriveInWorkbench", on);
    }

    public Rectangle getWindowState() {
        return windowState;
    }

    public void setWindowState(Rectangle windowState) {
        this.windowState = windowState;
    }
}

