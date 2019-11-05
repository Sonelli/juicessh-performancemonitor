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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ceylon.common.Constants;
import org.eclipse.ceylon.common.Versions;

public class LauncherUtil {
    private LauncherUtil() {}
    
    private static final String CEYLON_REPO = "repo";
    private static final String CEYLON_LIBS = "lib";
    
    // Can't use OSUtil.isWindows() here because these classes are put in the
    // ceylon-bootstrap.jar that doesn't have access to ceylon-common
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    
    public static File determineHome() throws URISyntaxException {
        // Determine the Ceylon home/install folder
        File ceylonHome;
        // First try the ceylon.home system property
        String ceylonHomeStr = System.getProperty(Constants.PROP_CEYLON_HOME_DIR);
        if (ceylonHomeStr == null) {
            // Second try to deduce it from the location of the current JAR file
            // (assuming either $CEYLON_HOME/lib/ceylon-bootstrap.jar or
            // $CEYLON_HOME/repo/ceylon/bootstrap/x.x.x/ceylon-bootstrap-x.x.x.jar)
            File jar = determineRuntimeJar();
            ceylonHome = jar.getParentFile().getParentFile();
            if (ceylonHome.getName().equals("bootstrap") && ceylonHome.getParentFile().getName().equals("ceylon")) {
                ceylonHome = ceylonHome.getParentFile().getParentFile().getParentFile();
            }
            if (!checkHome(ceylonHome)) {
                // Third try the CEYLON_HOME environment variable
                ceylonHomeStr = System.getenv(Constants.ENV_CEYLON_HOME_DIR);
                if (ceylonHomeStr == null) {
                    // As a last ditch effort see if we can find "ceylon" in the system's shell
                    // path and decuce the home folder from that (assuming $CEYLON_HOME/bin/ceylon)
                    File script = findCeylonScript();
                    if (script != null) {
                        ceylonHome = script.getParentFile().getParentFile();
                    }
                }
            }
        } else {
            ceylonHome = new File(ceylonHomeStr);
        }
        return ceylonHome;
    }

    public static File determineRepo(File ceylonHome) throws URISyntaxException {
        // Determine the Ceylon system repository folder
        File ceylonRepo;
        String ceylonSystemRepo = System.getProperty(Constants.PROP_CEYLON_SYSTEM_REPO);
        if (ceylonSystemRepo != null) {
            ceylonRepo = new File(ceylonSystemRepo);
        } else {
            ceylonRepo = new File(ceylonHome, CEYLON_REPO);
        }
        return ceylonRepo;
    }
    
    public static File determineLibs(File ceylonHome) throws URISyntaxException {
        // Determine the Ceylon system library folder
        File ceylonLib;
        String ceylonSystemRepo = System.getProperty(Constants.PROP_CEYLON_SYSLIBS_DIR);
        if (ceylonSystemRepo != null) {
            ceylonLib = new File(ceylonSystemRepo);
        } else {
            ceylonLib = new File(ceylonHome, CEYLON_LIBS);
        }
        return ceylonLib;
    }
    
    public static String determineSystemVersion() {
        // Determine the Ceylon system/language/runtime version
        String ceylonVersion = System.getProperty(Constants.PROP_CEYLON_SYSTEM_VERSION);
        if (ceylonVersion == null) {
            ceylonVersion = System.getenv(Constants.ENV_CEYLON_VERSION);
            if (ceylonVersion == null) {
                ceylonVersion = Versions.CEYLON_VERSION_NUMBER;
            }
        }
        return ceylonVersion;
    }
    
    public static File determineRuntimeJar() throws URISyntaxException {
        return new File(LauncherUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
    }
    
    private static File findCeylonScript() {
        String path = System.getenv("PATH");
        if (path != null) {
            String ceylonScriptName;
            if (IS_WINDOWS) {
                ceylonScriptName = "ceylon.bat";
            } else {
                ceylonScriptName = "ceylon";
            }
            String[] elems = path.split(File.pathSeparator);
            for (String elem : elems) {
                File script = new File(elem, ceylonScriptName);
                if (script.isFile() && script.canExecute() && isSameScriptVersion(script)) {
                    try {
                        // only if the version is compatible with this version!
                        return script.getCanonicalFile();
                    } catch (IOException e) {
                        // Ignore errors and keep on trying
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean isSameScriptVersion(File script) {
        List<String> args = new ArrayList<String>(4);
        if (IS_WINDOWS) {
            args.add("cmd.exe");
            args.add("/C");
        }
        args.add(script.getAbsolutePath());
        args.add("--version");
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try{
            Process process = processBuilder.start();
            InputStream in = process.getInputStream();
            InputStreamReader inread = new InputStreamReader(in);
            BufferedReader bufferedreader = new BufferedReader(inread);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedreader.readLine()) != null) {
                sb.append(line);
            }
            int exit = process.waitFor();
            bufferedreader.close();
            if(exit != 0)
                return false;
            return sb.toString().startsWith("ceylon version "+Versions.CEYLON_VERSION_MAJOR+"."+Versions.CEYLON_VERSION_MINOR);
        }catch(Throwable t){
            return false;
        }
    }

    private static boolean checkHome(File ceylonHome) {
        return (new File(ceylonHome, CEYLON_REPO)).isDirectory() && (new File(ceylonHome, CEYLON_LIBS)).isDirectory();
    }

    public static boolean hasArgument(final String[] args, final String test) {
        for (String arg : args) {
            if ("--".equals(arg)) {
                break;
            }
            if (arg.equals(test) || arg.startsWith(test + "=")) {
                return true;
            }
        }
        return false;
    }

    public static String getArgument(final String[] args, final String test, boolean optionalArgument) {
        for (int i=0; i < args.length; i++) {
            String arg = args[i];
            if ("--".equals(arg)) {
                break;
            }
            if (!optionalArgument && i < (args.length - 1) && arg.equals(test)) {
                return args[i + 1];
            }
            if (arg.startsWith(test + "=")) {
                return arg.substring(test.length() + 1);
            }
        }
        return null;
    }

    public static File absoluteFile(File file) {
        if (file != null) {
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                file = file.getAbsoluteFile();
            }
        }
        return file;
    }

}
