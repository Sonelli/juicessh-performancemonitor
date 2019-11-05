/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.ceylon.common.Constants;

public class Launcher {

    public static void main(String[] args) throws Throwable {
        // we don't need to clean up the class loader when run from main because the JVM will either exit, or
        // keep running with daemon threads in which case it will keep needing this classloader open 
        int exit = run(args);
        // WARNING: NEVER CALL EXIT IF WE STILL HAVE DAEMON THREADS RUNNING AND WE'VE NO REASON TO EXIT WITH A NON-ZERO CODE
        if(exit != 0)
            System.exit(exit);
    }

    public static int run(String... args) throws Throwable {
        return run(false, args);
    }

    public static int run(boolean cleanupClassLoader, String... args) throws Throwable {
        Java7Checker.check();
        CeylonClassLoader loader = getClassLoader();
        try{
            return runInJava7Checked(loader, args);
        }finally{
            if(cleanupClassLoader)
                loader.close();
        }
    }
    
    // FIXME: perhaps we should clear all the properties we set in there on exit?
    // this may not work for run, if they leave threads running 
    public static int runInJava7Checked(CeylonClassLoader loader, String... args) throws Throwable {
        // If the --sysrep option was set on the command line we set the corresponding system property
        String ceylonSystemRepo = LauncherUtil.getArgument(args, "--sysrep", false);
        if (ceylonSystemRepo != null) {
            System.setProperty(Constants.PROP_CEYLON_SYSTEM_REPO, ceylonSystemRepo);
        }

        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try{
            // This is mostly required by CeylonTool.getPluginLoader(), and perhaps by jboss modules
            Thread.currentThread().setContextClassLoader(loader);
            
            // We actually need to construct and set a new class path for the compiler
            // which doesn't use the actual class path used by the JVM but it constructs
            // it's own list looking at the arguments passed on the command line or
            // at the system property "env.class.path" which we will be using here.
            String cp = CeylonClassLoader.getClassPathAsString();
            System.setProperty("env.class.path", cp);

            // Find the main tool class
            String verbose = null;
            Class<?> mainClass = loader.loadClass("org.eclipse.ceylon.common.tools.CeylonTool");

            // Set up the arguments for the tool
            Object mainTool = mainClass.newInstance();
            Integer result;
            Method setupMethod = mainClass.getMethod("setup", args.getClass());
            try {
                result = (Integer)setupMethod.invoke(mainTool, (Object)args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            if (result == 0 /* SC_OK */) {
                try {
                    Method toolGetter = mainClass.getMethod("getTools");
                    Object[] tools = (Object[]) toolGetter.invoke(mainTool);
                    // just use the first one since they share args
                    if(tools != null && tools.length > 0){
                        Method verboseGetter = tools[0].getClass().getMethod("getVerbose");
                        verbose = (String)verboseGetter.invoke(tools[0]);
                    }
                } catch (Exception ex) {
                    // Probably doesn't have a --verbose option
                }

                //boolean verbose = hasArgument(args, "--verbose") && getArgument(args, "--verbose", true) == null;
                initGlobalLogger(verbose);

                try{
                    if (hasVerboseFlag(verbose, "loader")) {
                        Logger log = Logger.getLogger("org.eclipse.ceylon.log.loader");
                        log.info("Current directory is '" + LauncherUtil.absoluteFile(new File(".")).getPath() + "'");
                        log.info("Ceylon home directory is '" + LauncherUtil.determineHome() + "'");
                        for (File f : CeylonClassLoader.getClassPath()) {
                            log.info("path = " + f + " (" + (f.exists() ? "OK" : "Not found!") + ")");
                        }
                    }

                    // And finally execute the tool
                    Method execMethod = mainClass.getMethod("execute");
                    try {
                        result = (Integer)execMethod.invoke(mainTool);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }finally{
                    // make sure we reset it, otherwise it will keep a reference to the CeylonClassLoader
                    LogManager.getLogManager().reset();
                }
            }

            return result.intValue();
        }finally{
            // be sure to restore it to avoid memory leaks
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }

    public static CeylonClassLoader getClassLoader() throws ClassLoaderSetupException {
        try{
            // Create the class loader that knows where to find all the Ceylon dependencies
            CeylonClassLoader ceylonClassLoader = CeylonClassLoader.newInstance();

            // Set some important system properties
            initGlobalProperties();

            return ceylonClassLoader;
        }catch(URISyntaxException e){
            throw new ClassLoaderSetupException(e);
        }catch(MalformedURLException e){
            throw new ClassLoaderSetupException(e);
        }catch(FileNotFoundException e){
            throw new ClassLoaderSetupException(e);
        }
    }

    public static void initGlobalProperties() throws URISyntaxException {
        File ceylonHome = LauncherUtil.determineHome();
        initGlobalProperties(ceylonHome);
    }

    public static void initGlobalProperties(File ceylonHome) throws URISyntaxException {
        System.setProperty(Constants.PROP_CEYLON_HOME_DIR, ceylonHome.getAbsolutePath());
        System.setProperty(Constants.PROP_CEYLON_SYSTEM_REPO, LauncherUtil.determineRepo(ceylonHome).getAbsolutePath());
        System.setProperty(Constants.PROP_CEYLON_SYSTEM_VERSION, LauncherUtil.determineSystemVersion());
    }

    public static void initGlobalLogger(String verbose) {
        try {
            //if no log Manager specified use JBoss LogManager
            String logManager = System.getProperty("java.util.logging.manager");
            if (logManager == null) {
                System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
            }

            if (verbose != null) {
                String[] flags = verbose.split(",");
                for (String flag : flags) {
                    flag = flag.trim();
                    if ("all".equals(flag) || flag.isEmpty()) {
                        initLogger(Logger.getLogger(""), true);
                    } else if (flag.matches("^[a-z]+$")) {
                        initLogger(Logger.getLogger("org.eclipse.ceylon.log." + flag), true);
                    }
                }
            } else {
                initLogger(Logger.getLogger(""), false);
            }
        } catch (Throwable ex) {
            System.err.println("Warning: log configuration failed: " + ex.getMessage());
        }
    }

    private static void initLogger(Logger logger, boolean verbose) {
        boolean handlersExists = false;
        for (Handler handler : logger.getHandlers()) {
            handlersExists = true;

            //TODO Should we remove this hack? If handler are configured then levels should be too.
            // This is a hack, but at least it works. With a property file our log
            // formatter has to be in the boot class path. This way it doesn't.
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(CeylonLogFormatter.INSTANCE);
                if (verbose) {
                    handler.setLevel(Level.ALL);
                }
            }
        }
        if (verbose) {
            //TODO do not configure root logger, make it flags aware
            logger.setLevel(Level.ALL);
            if (handlersExists == false) {
                ConsoleHandler handler = new ConsoleHandler();
                handler.setFormatter(CeylonLogFormatter.INSTANCE);
                handler.setLevel(Level.ALL);
                logger.addHandler(handler);
            }
        }
    }
    
    // Returns true if one of the argument passed matches one of the flags given to
    // --verbose=... on the command line or if one of the flags is "all"
    private static boolean hasVerboseFlag(String verbose, String flag) {
        if (verbose == null) {
            return false;
        }
        if (verbose.isEmpty()) {
            return true;
        }
        List<String> lst = Arrays.asList(verbose.split(","));
        if (lst.contains("all")) {
            return true;
        }
        return lst.contains(flag);
    }
}
