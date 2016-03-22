
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
/*
 * Created on Feb 21, 2004
 *
 * Copyright Versant Corporation 2003-2005, All rights reserved
 */
package com.versant.core.vds.tools.jdo;

import java.util.*;
import java.net.MalformedURLException;

import com.versant.core.vds.tools.CommandLineParser;
import com.versant.core.vds.tools.Flag;
import com.versant.core.vds.VdsConnection;
import com.versant.core.vds.VdsStorageManagerFactory;

import com.versant.core.common.Debug;
import com.versant.core.common.config.ConfigInfo;
import com.versant.core.common.config.ConfigParser;
import com.versant.core.metadata.ModelMetaData;
import com.versant.core.storagemanager.StorageManagerFactoryBuilder;
import com.versant.odbms.DatastoreManager;
import com.versant.odbms.model.*;
import com.versant.odbms.model.schema.*;


/**
 * SchemaTool lets you define, compare or evolve datastore schema from a set
 * of enhanced Java classes.
 *
 * @author ppoddar
 *
 */
public class SchemaTool extends AbstractJdoTool {
	/** Uses this class loader to load metadata and domain classes.
	 *  By default it is <code>ClassLoader.getSystemClassLoader()</code>
	 *  but can be set by the caller using {link #setClassLoader(String[])}
	 */
	private ClassLoader _classLoader = ClassLoader.getSystemClassLoader();

	/** Main entry point for this tool under a command-line invocation.
	 * <p>
	 * Get help on usage by
	 * <p>
	 * <code>
	 * java com.versant.core.vds.tools.tools.SchemaTool [-h|-?|-help]
	 * </code>
	 *
	 * @param args arguments for command-line. To see the description of
	 * the arguments try
	 * <p>
	 * <code>
	 * java com.versant.core.vds.tools.tools.SchemaTool [-h|-?|-help]
	 * </code>
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CommandLineParser clp = new CommandLineParser();

		Flag[] flags = { 
		        new Flag(new String[] { "-a", "-action" }, 	// aliases
				        false, 								// not optional
				        Flag.VALUE_MUST, 					// must have a value
						false, 								// case insensitive
						new String[] { "define", "compare", "evolve" }, // allowed values
						null, 								// default value
						"Specifies action"), 				// help description

				new Flag(new String[] { "-cp", "-classpath" }, 	// aliases
						true, 									// optional
						Flag.VALUE_MUST, 						// must have a value
						false, 									// case insensitive
						null, 									// allowed values
						null, 									// default value
						"Specifies classpath to load domain classes"), // help description

				new Flag(new String[] { "-p", "-properties" }, // aliases
						true, // not optional
						Flag.VALUE_MUST, // value constraint
						true, // case insensitive
						null, // any allowed values
						"versant.properties", // default value
						"Specifies jdogenie configuration file"), // help description

				new Flag(new String[] { "-out" }, // aliases
						true, // optional
						Flag.VALUE_OPTIONAL, // may have a value
						false, // case insensitive
						null, // any allowed value
						"stdout", // default value
						"Specifies output file"), // help description

				new Flag(new String[] { "-v", "-verbose" }, // aliases
						true, // optional
						Flag.VALUE_NEVER, // never has value
						false, // case insensitive
						null, // allowed values
						"false", // default value
						"trace"), // help description

				new Flag(new String[] { "-h", "-?", "-help" }, // aliases
						true, // optional
						Flag.VALUE_NEVER, // value constraint
						false, // case sensitive
						null, // allowed values
						null, // default value
						"Prints this help message"), // help description
		};
		clp.setFlags(flags);
		clp.setIgnoreCase(true);
		clp.setUsageCommand("Usage: java com.versant.core.vds.tools.tools.SchemaTool [options]");
		clp.setMinimumParamaterCount(1);

		clp.parse(args);
		SchemaTool tool = new SchemaTool();
		tool.run(clp);

	}

	/**
	 *
	 */
	public SchemaTool() {
		super();
	}

	/** Runs this tool based on <code>-action</code> flag.
	 * <p>
	 * <LI> sets the class loader if a classpath is specified.
	 * <LI> loads the configuration from a jdogenie properties file.
	 * <LI> imports JDO classes to datastore.
	 *
	 * @param clp
	 * @throws Exception
	 */
	void run(CommandLineParser clp) throws Exception {
		String action = clp.getFlag("-action").getValue();

		if (clp.getFlag("-classpath").isSet()) {
			setClassLoader(clp.getFlag("-classpath").getValue());
		}
		String propertiesFileName = clp.getFlag("-properties").getValue();
		Properties pConfig = loadProperties(propertiesFileName);
		ConfigInfo config = new ConfigParser().parse(pConfig);

		boolean evolve = action.equals("evolve");
		boolean compare = action.equals("compare");
		importJDO(config, evolve, compare);
	}

	/** Imports JDO class definition to datastore. May modify the existing datastore
	 * definitions to suit the JDO class definitions.
	 * <p>
	 * <B>Note</B>: Schema modification <em>may</em> result in loss of data for instances
	 * that conform to older class definitions.
	 * <p>
	 * The domain classes specified in JDO metadata are loaded using the current
	 * classloader of this receiver. This classloader is, by default, the loader
	 * that loaded this receiver itself. But can be changed using {@link #setClassLoader(String[])}.
	 * <p>
	 * This method operates with in its own transaction. A new transaction is started
	 * and committed if no schema conflict is detected or <code>evolve</code> is set
	 * to true. Otherwise the transaction is rolled back.
	 *
	 * @param config Configuration provides database URL where the class definitions
	 * to be persisted and the naming policy to be used in mapping Java class names
	 * to schema names.
	 * @param evolve flag controls the behaviour when JDO class definitions conflict
	 * with persistent class definitions in the datastore. Setting this flag to true,
	 * will update the persistent schema to match the JDO class definitions.
	 * If the flag is set to false then the schema changes required are calculated
	 * but not applied.
	 * @param compare flags that schema should be compared but no definitive action should be taken.
	 * <p>
	 * Either <code>evolve</code> or <code>compare</code> can be true but not both.
	 * Both can be false, implying action as <code>define</code>.
	 *
	 * @return A collection of schema changes that are required to match the JDO class
	 * definitions to persistent definitions. These changes are applied or not based
	 * on the <code>evolve</code> flag. The new class definitions are not reported
	 * as schema changes (will be in future).
	 *
	 */
	public SchemaEditCollection importJDO(
		final ConfigInfo config,
	    boolean evolve,
        boolean compare) throws Exception {

//		assert config!=null;
//	    assert !(evolve&compare);
        if (Debug.DEBUG) {
            Debug.assertInternal(config != null,
                    "ConfigInfo is null");
            Debug.assertInternal(!(evolve & compare),
                    "assert !(evolve & compare)");
        }

        // Load the JDO schema. As a side effect, Versant user schema
        // model is also built.
        StorageManagerFactoryBuilder b = new StorageManagerFactoryBuilder();
        b.setConfig(config);
        b.setLoader(getClassLoader());
        b.setFullInit(false);
        b.setIgnoreConFactoryProperties(true);
        VdsStorageManagerFactory smf = (VdsStorageManagerFactory)
            b.createStorageManagerFactory().getInnerStorageManagerFactory();
	    ModelMetaData jmd = smf.getModelMetaData();
	    UserSchemaModel userModel = (UserSchemaModel)jmd.vdsModel;

	    // connect to Versant data store specified in the config.
	    VdsConnection pooledConnection = smf.getPool().getConnection(false);
	    DatastoreManager rpc = (DatastoreManager)pooledConnection.getCon();

		SchemaEditor editor = rpc.getSchemaEditor();
		SchemaEditCollection edits = editor.define(userModel, false, SchemaEditor.NO_OPTIONS);
		String header = "";
		if (evolve) {
		    if (edits.isEvolutionRequired()) editor.evolve(userModel, edits);
		    header = "Schema has evolved with following changes:";
			rpc.commitTransaction();
		} else if (compare){
		    header = "Schema comparison has detected following difference(s) but not committed\r\n";
		    if (edits.isEvolutionRequired()) 
		           header += "Use -action evolve to apply these changes:";
		    rpc.rollbackTransaction();
		} else { // define 
		    if (edits.isEvolutionRequired()) {
			    header = "Schema can not be defined as following difference(s) have been detected.\r\n"+
			             "Use -action evolve to apply these changes:";
		        rpc.rollbackTransaction();
		    } else {
			    header = "Following schema has been defined:";
		        rpc.commitTransaction();
		    }
		}
		prettyPrint(header, userModel,edits);
		
		return edits;
	}
	void prettyPrint(String header, UserSchemaModel userModel, SchemaEditCollection edits){
		SchemaEdit[] orderedEdits = edits.getOrderedEdits(userModel);
		String[] undefinedClassNames = edits.getUndefinedClasses();
		String[] droppedClassNames = edits.getDroppedClasses();
		
		if (edits.size()>0) {
		    System.out.println("\r\n"+ header);
		} else {
		    System.out.println("No schema changes required");
		}
		if (undefinedClassNames.length>0) System.out.println("New class(es) created:");
		for (int i=0; i<undefinedClassNames.length; i++){
		    System.out.println("\t" + undefinedClassNames[i]);
		}
		if (droppedClassNames.length>0) System.out.println("Class(es) to be deleted:");
		for (int i=0; i<droppedClassNames.length; i++){
		    System.out.println("\t" + droppedClassNames[i]);
		}
		if (orderedEdits.length>0) System.out.println("Schema change(s):");
		for (int i=0; i<orderedEdits.length; i++){
		    System.out.println("\t" + orderedEdits[i]);
		    
		}
	}
	
	/** Gets an array of persistent classes given a user defined model.
	 * User defined class and persistent classes use the same namespace,
	 * so each persistent class is looked up by corresponding user class
	 * contained in the given model.
	 *
	 * @param rpc datastore connection to look up persistent classes.
	 * @param m user class model
	 * @return
	 */

	/** Gets the current class loader used by this receiver to load metadata
	 * resources such as enahnced Java classes or JDO metadata files.
	 *
	 * @return a ClassLoader. never null.
	 */
	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	/** Sets a private classloader given an array of classpath segments.
	 * The class loader is a child of the current classloader.
	 *
	 * @param classpaths
	 * @throws MalformedURLException
	 */
	public void setClassLoader(String[] classpaths)
			throws MalformedURLException {
		_classLoader = new FileURLClassLoader(classpaths, _classLoader);
	}

	/** Sets a private classloader given a classpath separated by
	 * <code>File.pathSeparator</code>
	 *
	 * @param classpath
	 * @throws MalformedURLException
	 */
	public void setClassLoader(String classpath) throws MalformedURLException {
		_classLoader = new FileURLClassLoader(classpath, _classLoader);
	}

}
