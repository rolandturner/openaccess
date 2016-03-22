
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
package com.versant.core.jdo.tools.workbench.model;

import org.jdom.Element;

import org.jdom.input.SAXBuilder;


import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import com.versant.core.metadata.parser.JdoExtension;
import com.versant.core.metadata.parser.JdoExtensionKeys;
import com.versant.core.metadata.parser.MetaDataParser;
import com.versant.core.util.CharBuf;

import javax.jdo.PersistenceManager;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.awt.Color;

/**
 */
public class MdQuery implements JdoExtensionKeys, Comparable {

    private static final String ICON_JDOQL = "JDOQL16.gif";
    private static final String ICON_SQL = "SQL16.gif";

    private MdElement queryElement;
    private String candidateClass;
    private MdProject project;
    private MdClass mdClass;
    private List params = new ArrayList();
    private boolean error;
    private String errorMsg;
    private String fileName;

    public MdQuery(MdProject project, MdClass mdClass, MdElement element) {
        this.mdClass = mdClass;
        queryElement = element;
        candidateClass = mdClass.getQName();
        this.project = project;
        updateMdClass();
        initParams();
    }

    public MdQuery(MdProject project) {
        setProject(project);
        createElement();
        setName("untitledQuery-" + (project.getQueries(null).size() + 1));
    }

    private void initParams() {
        String parameters = getParameters();
        if (parameters != null) {
            StringTokenizer token = new StringTokenizer(parameters, ",");
            Iterator iter = getParamValues().iterator();
            params.clear();
            while (token.hasMoreTokens()) {
                String param = token.nextToken();
                Object value = null;
                if (iter.hasNext()) {
                    value = iter.next();
                }
                ParamAndValue paramAndValue = new ParamAndValue(this);
                paramAndValue.setPrivateDeclaration(param);
                paramAndValue.setPrivateValue(value);
                params.add(paramAndValue);
            }
        }
    }

    public void redoParams() {
        setParameters(getParamString());
        setParamValues(getValuesAsStrings());
    }

    /**
     * Creates a document with all the elements of our JDOQL query.
     */
    private void createElement() {
        queryElement = new MdElement("query");
    }

    public boolean isDirty() {
        return false;
    }

    public MdProject getProject() {
        return project;
    }

    public void setProject(MdProject project) {
        if (this.project == project) return;
        this.project = project;
        updateMdClass();
    }

    public String getCandidateClassStr() {
        return candidateClass;
    }

    public MdValue getCandidateClass() {
        MdValue v = new MdValue(candidateClass);
        if (project != null) {
            v.setPickList(project.getAllClassNames());
        } else {
            v.setOnlyFromPickList(false);
        }
        v.setColor(Color.black);
        return v;
    }

    public void setCandidateClass(MdValue v) throws Exception {
        setCandidateClass(v.getText());
    }

    public void setCandidateClass(String s) throws Exception {
        if (isInMetaData()) {
            MdClass oldMdClass = project.findClass(candidateClass);
            if (oldMdClass != null) {
                oldMdClass.removeMdQuery(this);
            }
            MdClass newMdClass = project.findClass(s);
            if (newMdClass != null) {
                newMdClass.addMdQuery(this);
                mdClass = newMdClass;
                candidateClass = s;
            }
        } else {
            MdClass newMdClass = project.findClass(s);
            if (newMdClass != null) {
                mdClass = newMdClass;
                candidateClass = s;
            }
        }
    }

    /**
     * Is this query stored in the metadata
     *
     * @return
     */
    public boolean isInMetaData() {
        if (mdClass == null) {
            return false;
        } else {
            return mdClass.getQueries(null).contains(this);

        }
    }

    public boolean enableAddToMetadata() {
        return !isInMetaData();
    }

    public void addToMetaData() {
        if (mdClass != null) {
            mdClass.addMdQuery(this);
        }
    }

    public void updateMdClass() {
        if (project != null && candidateClass != null) {
            mdClass = project.findClass(candidateClass);
        } else {
            mdClass = null;
        }
    }

    public MdClass getMdClass() {
        return mdClass;
    }

    public boolean isSql() {
        return MdUtils.isStringNotEmpty(getSql());
    }

    /**
     * Get the values of all params. This will convert OidWrapper's into
     * OIDs and String's into the correct types to match the parameters.
     */
    
    public Object[] getParamValues(PersistenceManager pm, String dateFormat)
            throws Exception {
        int n = params.size();
        Object[] oa = new Object[n];
        for (int i = 0; i < n; i++) {
            ParamAndValue p = (ParamAndValue)params.get(i);
            Object v = p.getResolvedValue(pm, dateFormat);
            if (v instanceof OidWrapper) v = ((OidWrapper)v).getOid(); //todo
            oa[i] = v;
        }
        return oa;
    }
     

    /**
     * Get the values of all params.
     */
    public String[] getValuesAsStrings() {
        int n = params.size();
        String[] oa = new String[n];
        for (int i = 0; i < n; i++) {
            ParamAndValue p = (ParamAndValue)params.get(i);
            Object v = p.getValue();
            oa[i] = (String)v;
        }
        return oa;
    }

    public String getParamStringValues(String dateFormat) throws Exception {
        int n = params.size();
        CharBuf buff = new CharBuf();
        for (int i = 0; i < n; i++) {
            ParamAndValue p = (ParamAndValue)params.get(i);
            String s = p.getResolvedString(dateFormat);
            buff.append(s);
        }
        return buff.toString();
    }

    public String getObjectArrayStringValues() throws Exception {
        int n = params.size();
        if (n <= 3) return "";
        CharBuf buff = new CharBuf(
                "Object[] objArray = new Object[" + n + "];\n");
        for (int i = 0; i < n; i++) {
            ParamAndValue p = (ParamAndValue)params.get(i);
            String s = p.getStringName();
            buff.append("objArray[" + i + "] = " + s + ";\n");
        }
        return buff.toString();
    }

    /**
     * Get the parameter declarations in a comma separated String.
     */
    public String getParamString() {
        int n = params.size();
        if (n == 0) return null;
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < n; i++) {
            if (i > 0) s.append(", ");
            ParamAndValue p = (ParamAndValue)params.get(i);
            String declaration = p.getDeclaration();
            if (MdUtils.isStringNotEmpty(declaration)) {
                s.append(declaration);
            }
        }
        return s.toString();
    }

    public List getParams() {
        return params;
    }

    public void addParam() {
        params.add(new ParamAndValue(this));
    }

    public void removeParam(ParamAndValue p) {
        params.remove(p);
        redoParams();
    }

    private ArrayList getParamValues() {
        ArrayList valueList = new ArrayList();
        MdElement values = XmlUtils.findExtension(queryElement,
                QUERY_PARAM_VALUES);
        if (values != null) {
            List list = XmlUtils.findExtensions(values, VALUE);
            for (Iterator i = list.iterator(); i.hasNext();) {
                MdElement e = (MdElement)i.next();
                valueList.add(e.getAttributeValue("value"));
            }
        }
        return valueList;
    }

    private void setParamValues(String[] values) {
        if (values.length == 0) {
            XmlUtils.removeExtensions(queryElement, QUERY_PARAM_VALUES);
        } else {
            MdElement queryValues = XmlUtils.findOrCreateExtension(
                    queryElement, QUERY_PARAM_VALUES);
            queryValues.removeContent(); // clear all extension
            for (int i = 0; i < values.length; i++) {
                MdElement e = new MdElement("extension");
                e.setAttribute("vendor-name", MetaDataParser.VENDOR_NAME);
                e.setAttribute("key", JdoExtension.toKeyString(VALUE));
                if (values[i] != null) {
                    e.setAttribute("value", values[i]);
                }
                queryValues.addContent(e);
            }
        }

    }

    public String getComment() {
        return XmlUtils.getComment(queryElement);
    }

    public void setComment(String comment) {
        XmlUtils.setComment(queryElement, comment);
    }

    public MdElement getElement() {
        return queryElement;
    }

    public void setElement(MdElement element) {
        this.queryElement = element;
    }

    public void setName(int queryCount) {
        setName(
                "untitledQuery-" + (project.getQueries(null).size() + 1 + queryCount));
    }

    // ---------------- all query attributes ---------------------------
    public String getName() {
        return queryElement.getAttributeValue("name");
    }

    public void setName(String name) {
        XmlUtils.setAttribute(queryElement, "name", name);
    }

    public String getLanguage() {
        return queryElement.getAttributeValue("language");
    }

    public void setLanguage(String language) {
        XmlUtils.setAttribute(queryElement, "language", language);
    }

    public MdValue getIgnoreCache() {
        MdValue value = new MdValue(
                queryElement.getAttributeValue("ignore-cache"));
        value.setOnlyFromPickList(true);
        value.setPickList(PickLists.BOOLEAN);
        return value;
    }

    public void setIgnoreCache(MdValue ignore_cache) {
        String value = ignore_cache.getText();
        if (MdUtils.isStringNotEmpty(value)) {
            setIgnoreCache(value);
        } else {
            setIgnoreCache((String)null);
        }
    }

    public void setIgnoreCache(String ignore_cache) {
        XmlUtils.setAttribute(queryElement, "ignore-cache", ignore_cache);
    }

    public MdValue getIncludeSubclasses() {
        MdValue value = new MdValue(
                queryElement.getAttributeValue("include-subclasses"));
        value.setOnlyFromPickList(true);
        value.setPickList(PickLists.BOOLEAN);
        value.setDefText("true");
        return value;
    }

    public void setIncludeSubclasses(MdValue include_subclasses) {
        String value = include_subclasses.getText();
        if (MdUtils.isStringNotEmpty(value)) {
            setIncludeSubclasses(value);
        } else {
            setIncludeSubclasses((String)null);
        }
    }

    public void setIncludeSubclasses(String include_subclasses) {
        XmlUtils.setAttribute(queryElement, "include-subclasses",
                include_subclasses);
    }

    public String getOrder() {
        return queryElement.getAttributeValue("ordering");
    }

    public void setOrder(String ordering) {
        XmlUtils.setAttribute(queryElement, "ordering", ordering);
    }

    public String getRange() {
        return queryElement.getAttributeValue("range");
    }

    public void setRange(String range) {
        XmlUtils.setAttribute(queryElement, "range", range);
    }

    // -------------------------- filter stuff ------------------------------
    public String getFilter() {
        String filter = queryElement.getAttributeValue("filter");
        if (filter != null) {
            setFilter(filter);
            queryElement.setAttribute("filter", "");
        }

        return XmlUtils.getText(queryElement, "filter");
    }

    public void setFilter(String filter) {
        XmlUtils.setText(queryElement, "filter", filter);
    }

    // -------------------------- sql stuff ------------------------------
    public String getSql() {
        String sql = queryElement.getAttributeValue("sql");
        if (sql != null) {
            setSql(sql);
            queryElement.removeAttribute("sql");
        }
        return XmlUtils.getText(queryElement, "sql");
    }

    public void setSql(String sql) {
        XmlUtils.setText(queryElement, "sql", sql);
    }

    // ------------------------- all result stuff -----------------------------
    public String getGrouping() {
        return XmlUtils.getAttribute(queryElement, "result", "grouping");
    }

    public void setGrouping(String grouping) {
        XmlUtils.setAttribute(queryElement, "result", "grouping", grouping);
    }

    public String getResultClass() {
        return XmlUtils.getAttribute(queryElement, "result", "class");
    }

    public void setResultClass(String resultClass) {
        XmlUtils.setAttribute(queryElement, "result", "class", resultClass);
    }

    public MdValue getUnique() {
        MdValue value = new MdValue(
                XmlUtils.getAttribute(queryElement, "result", "unique"));
        value.setOnlyFromPickList(true);
        value.setPickList(PickLists.BOOLEAN);
        return value;
    }

    public void setUnique(MdValue unique) {
        String value = unique.getText();
        if (MdUtils.isStringNotEmpty(value)) {
            setUnique(value);
        } else {
            setUnique((String)null);
        }
    }

    public void setUnique(String unique) {
        XmlUtils.setAttribute(queryElement, "result", "unique", unique);
    }

    public String getResult() {
        return XmlUtils.getText(queryElement, "result");
    }

    public void setResult(String result) {
        XmlUtils.setText(queryElement, "result", result);
    }

    // ------------------  all declaration stuff  ----------------------------
    public String getImports() {
        return XmlUtils.getAttribute(queryElement, "declare", "imports");
    }

    public void setImports(String imports) {
        XmlUtils.setAttribute(queryElement, "declare", "imports", imports);
    }

    public String getParameters() {
        return XmlUtils.getAttribute(queryElement, "declare", "parameters");
    }

    public void setParameters(String parameters) {
        XmlUtils.setAttribute(queryElement, "declare", "parameters",
                parameters);
    }

    public String getVariables() {
        return XmlUtils.getAttribute(queryElement, "declare", "variables");
    }

    public void setVariables(String variables) {
        XmlUtils.setAttribute(queryElement, "declare", "variables", variables);
    }
    //--------------------------------------------------------------------------

    public MdValue getFetchGroup() {
        MdValue v = createMdValue(FETCH_GROUP);
        if (mdClass != null) {
            v.setPickList(mdClass.getFetchGroupNameList());
        }
        return v;
    }

    public String getFetchGroupString() {
        return getStringValue(FETCH_GROUP);
    }

    public void setFetchGroup(MdValue v) {
        setStringValue(FETCH_GROUP, v);
    }

    public void setFetchGroup(String v) {
        setStringValue(FETCH_GROUP, v);
    }

    public MdValue getMaxRows() {
        return createMdValueInt(MAX_ROWS);
    }

    public int getMaxRowsInt() {
        return getIntValue(MAX_ROWS);
    }

    public void setMaxRows(MdValue v) {
        setStringValue(MAX_ROWS, v);
    }

    public void setMaxRows(int v) {
        setStringValueInt(MAX_ROWS, v);
    }

    public MdValue getFetchSize() {
        return createMdValueInt(FETCH_SIZE);
    }

    public int getFetchSizeInt() {
        return getIntValue(FETCH_SIZE);
    }

    public void setFetchSize(MdValue v) {
        setStringValue(FETCH_SIZE, v);
    }

    public void setFetchSize(int v) {
        setStringValueInt(FETCH_SIZE, v);
    }

    public MdValue getBounded() {
        return createMdValueBoolean(BOUNDED);
    }

    public boolean getBoundedBoolean() {
        return getBoolean(getStringValue(BOUNDED));
    }

    public void setBounded(MdValue v) {
        setStringValue(BOUNDED, v);
    }

    public MdValue getOptimistic() {
        return createMdValueBoolean(OPTIMISTIC);
    }

    public boolean getOptimisticBoolean() {
        return getBoolean(getStringValue(OPTIMISTIC));
    }

    public void setOptimistic(MdValue v) {
        setStringValue(OPTIMISTIC, v);
    }

    public MdValue getCountStarOnSize() {
        return createMdValueBoolean(COUNT_STAR_ON_SIZE);
    }

    public boolean getCountStarOnSizeBoolean() {
        return getBoolean(getStringValue(COUNT_STAR_ON_SIZE));
    }

    public void setCountStarOnSize(MdValue countStarOnSize) {
        setStringValue(COUNT_STAR_ON_SIZE, countStarOnSize);
    }

    public MdValue getRandomAccess() {
        return createMdValueBoolean(RANDOM_ACCESS);
    }

    public boolean getRandomAccessBoolean() {
        return getBoolean(getStringValue(RANDOM_ACCESS));
    }

    public void setRandomAccess(MdValue randomAccess) {
        setStringValue(RANDOM_ACCESS, randomAccess);
    }

    private boolean getBoolean(String name) {
        return ((name != null) && name.equalsIgnoreCase("true"));
    }

    private String getStringValue(int key) {
        MdElement element = XmlUtils.findExtension(queryElement, key);
        if (element != null) {
            String value = element.getAttributeValue("value");
            if (value == null || value.trim().equals("")) {
                return null;
            } else {
                return value;
            }
        }
        return null;
    }

    private int getIntValue(int key) {
        MdElement element = XmlUtils.findExtension(queryElement, key);
        if (element != null) {
            String value = element.getAttributeValue("value");
            if (value == null || value.trim().equals("")) {
                return 0;
            } else {
                return Integer.parseInt(value);
            }
        }
        return 0;
    }

    private void setStringValue(int key, MdValue v) {
        String value = v.getText();
        if (value == null || value.equals("")) {
            XmlUtils.removeExtensions(queryElement, key);
        } else {
            MdElement element = XmlUtils.findOrCreateExtension(queryElement,
                    key);
            element.setAttribute("value", value);
        }
    }

    private void setStringValue(int key, String value) {
        if (value == null || value.equals("")) {
            XmlUtils.removeExtensions(queryElement, key);
        } else {
            MdElement element = XmlUtils.findOrCreateExtension(queryElement,
                    key);
            element.setAttribute("value", value);
        }
    }

    private void setStringValueInt(int key, int v) {
        String value = "" + v;
        if (value == null || value.equals("")) {
            XmlUtils.removeExtensions(queryElement, key);
        } else {
            MdElement element = XmlUtils.findOrCreateExtension(queryElement,
                    key);
            element.setAttribute("value", value);
        }
    }

    private MdValue createMdValue(int key) {
        return new MdValue(getStringValue(key));
    }

    private MdValue createMdValueInt(int key) {
        MdValueInt value = new MdValueInt(getStringValue(key));
        value.setDefText("0");
        return value;
    }

    private MdValue createMdValueBoolean(int key) {
        MdValue value = new MdValue(getStringValue(key));
        value.setOnlyFromPickList(true);
        value.setPickList(PickLists.BOOLEAN);
        value.setDefText("false");
        return value;
    }

    public String toString() {
        getComment();
        StringBuffer buffer = new StringBuffer();
        buffer.append("\nname = ");
        buffer.append(getName());
        buffer.append("\nlanguage = ");
        buffer.append(getLanguage());
        buffer.append("\nignore-cache = ");
        buffer.append(getIgnoreCache());
        buffer.append("\nfilter = ");
        buffer.append(getFilter());
        buffer.append("\nimports = ");
        buffer.append(getImports());
        buffer.append("\nparameters = ");
        buffer.append(getParameters());
        buffer.append("\nvariables = ");
        buffer.append(getVariables());
        buffer.append("\nordering = ");
        buffer.append(getOrder());
        buffer.append("\nresultClass = ");
        buffer.append(getResultClass());
        buffer.append("\ngrouping = ");
        buffer.append(getGrouping());
        buffer.append("\nunique = ");
        buffer.append(getUnique());

        return buffer.toString();
    }

    private String get(Element e, String name) {
        e = e.getChild(name);
        if (e == null) return null;
        return e.getText();
    }

    private int getInt(Element e, String name) {
        String s = get(e, name);
        if (s == null) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e1) {
            return 0;
        }
    }

    public String getFilename() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Load from a file.
     */
    public void load(String filename) throws Exception {
        this.fileName = filename;
        
        SAXBuilder b = new SAXBuilder(false);
        b.setFactory(MdJDOMFactory.getInstance());
        b.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId,
                    String systemId) {
                StringReader reader = new StringReader("");
                return new InputSource(reader);
            }
        });

 
        MdDocument doc = (MdDocument)b.build(filename);
        MdElement root = (MdElement)doc.getRootElement();
        if (root.getName().equals("jdoql")) {// old query format
            // Set all our query properties
            setOrder(get(root, "order"));
            setCandidateClass(get(root, "class"));
            setImports(get(root, "imports"));
            setFetchGroup(get(root, "fetchgroup"));
            setFilter(get(root, "filter"));
            setVariables(get(root, "variables"));
            String maxrows = get(root, "maxrows");
            if (maxrows != null) {
                setMaxRows(getInt(root, "maxrows"));
            }
            String fetchsize = get(root, "fetchsize");
            if (fetchsize != null) {
                setFetchSize(getInt(root, "fetchsize"));
            }

            // Set our query parameter properties
            params.clear();
            Element pc = root.getChild("parameters");
            if (pc != null) {
                for (Iterator i = pc.getChildren("param").iterator();
                     i.hasNext();) {
                    Element e = (Element)i.next();
                    ParamAndValue p = new ParamAndValue(this);
                    p.setDeclaration(e.getText());
                    Iterator j = e.getChildren("value").iterator();
                    if (j.hasNext()) {
                        p.setValue(((Element)j.next()).getText());
                    }
                    params.add(p);
                }
                redoParams();
            }
        } else if (root.getName().equals("jdo")) {
            MdElement pack = (MdElement)root.getChild(ModelConstants.PACKAGE_NODE_NAME);
            String packName = pack.getAttributeValue("name");
            MdElement clazz = (MdElement)pack.getChild("class");
            String className = clazz.getAttributeValue("name");
            String qname = packName + "." + className;

            MdClass currentMdClass = project.findClass(qname);
            if (currentMdClass != null) {
                this.mdClass = currentMdClass;
            }
            MdElement query = (MdElement)clazz.getChild("query");
            if (query != null) {
                queryElement = query;
            } else {
                queryElement = new MdElement("query");
            }
            candidateClass = qname;
            initParams();
        }
    }

    /**
     * Save to our file.
     */
    public void save() throws Exception {
        XMLOutputter outPutter = new XMLOutputter(XmlUtils.XML_FORMAT);
        File file = new File(fileName);
        FileWriter fileOut = new FileWriter(file);
        outPutter.output(createDocument(), fileOut);
        fileOut.close();
    }

    private MdDocument createDocument() {
        MdElement classElem = (MdElement)queryElement.getParent();
        if (classElem != null) {
            classElem.removeContent(queryElement);
        }
//        if (queryElement.getParent() == null){
        MdElement root = new MdElement("jdo");
        MdElement packElement = new MdElement(ModelConstants.PACKAGE_NODE_NAME);
        packElement.setAttribute("name",
                MdUtils.getPackage(mdClass.getQName()));
        root.addContent(packElement);
        MdElement classElement = new MdElement("class");
        classElement.setAttribute("name", MdUtils.getClass(mdClass.getQName()));
        packElement.addContent(classElement);
        classElement.addContent(queryElement);
        return new MdDocument(root);
//        } else {
//            MdElement classElem = (MdElement) queryElement.getParent();
//            if (classElem != null) {
//                classElem.removeContent(queryElement);
//            }
//            MdElement classElement = (MdElement)queryElement.getParent();
//            classElement.setAttribute("name", Utils.getClass(mdClass.getQName()));
//            MdElement packElement = (MdElement) classElement.getParent();
//            packElement.setAttribute("name", Utils.getPackage(mdClass.getQName()));
//            MdElement root = (MdElement) packElement.getParent();
//            return (MdDocument)root.getDocument();
//        }
    }

    /**
     * Order class by name.
     */
    public int compareTo(Object o) {
        String a = getName();
        String b = ((MdQuery)o).getName();
        return a.compareTo(b);
    }

    public String getTreeIcon() {
        return isSql() ? ICON_SQL : ICON_JDOQL;
    }

    public boolean hasErrors() {
        return error;
    }

    public void setErrors(boolean error) {
        this.error = error;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return MdUtils.getErrorHtml(errorMsg);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MdQuery)) return false;

        final MdQuery mdQuery = (MdQuery)o;

        if (candidateClass != null ? !candidateClass.equals(
                mdQuery.candidateClass) : mdQuery.candidateClass != null) {
            return false;
        }
        if (mdClass != null ? !mdClass.equals(mdQuery.mdClass) : mdQuery.mdClass != null) return false;
        String name1 = getName();
        String name2 = mdQuery.getName();
        if (name1 != null ? !name1.equals(name2) : name2 != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (candidateClass != null ? candidateClass.hashCode() : 0);
        result = 29 * result + (mdClass != null ? mdClass.hashCode() : 0);
        String name = getName();
        result = 29 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
