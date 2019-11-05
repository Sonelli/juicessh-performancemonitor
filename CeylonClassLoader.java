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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ceylon.common.Versions;

/**
 * Ceylon-specific class loader that knows how to find and add
 * all needed dependencies for compiler and runtime.
 * Implements child-first class loading to prevent mix-ups with
 * Java's own tool-chain.
 *
 * @author Tako Schotanus
 *
 */
public class CeylonClassLoader extends URLClassLoader {

    public static CeylonClassLoader newInstance() throws URISyntaxException, MalformedURLException, FileNotFoundException {
        return new CeylonClassLoader(getClassPath());
    }

    public static CeylonClassLoader newInstance(List<File> classPath) throws URISyntaxException, MalformedURLException, FileNotFoundException {
        return new CeylonClassLoader(classPath);
    }

    private String signature;
    
    private CeylonClassLoader(List<File> classPath) throws URISyntaxException, MalformedURLException, FileNotFoundException {
        super(toUrls(classPath));
        this.signature = toString(classPath);
    }

    private CeylonClassLoader(List<File> classPath, ClassLoader parentLoader) throws URISyntaxException, MalformedURLException, FileNotFoundException {
        super(toUrls(classPath), parentLoader);
        this.signature = toString(classPath);
    }

    public String getSignature(){
        return signature;
    }
    
    public boolean hasSignature(String signature){
        return signature != null && this.signature.equals(signature);
    }
    
    private static URL[] toUrls(List<File> cp) throws MalformedURLException {
        URL[] urls = new URL[cp.size()];
        int i = 0;
        for (File f : cp) {
            urls[i++] = f.toURI().toURL();
        }
        return urls;
    }

    private static String toString(List<File> cp) {
        StringBuilder classPath = new StringBuilder();
        for (File f : cp) {
            if (classPath.length() > 0) {
                classPath.append(File.pathSeparatorChar);
            }
            classPath.append(f.getAbsolutePath());
        }
        return classPath.toString();
    }

    public static String getClassPathAsString() throws URISyntaxException, FileNotFoundException {
        return toString(getClassPath());
    }

    public static String getClassPathSignature(List<File> cp) {
        return toString(cp);
    }

    public static List<File> getClassPath() throws URISyntaxException, FileNotFoundException {
        // Determine the necessary folders
        File ceylonHome = LauncherUtil.determineHome();
        File ceylonRepo = LauncherUtil.determineRepo(ceylonHome);

        // Perform some sanity checks
        checkFolders(ceylonHome, ceylonRepo);

        List<File> archives = new LinkedList<File>();

        // List all the necessary Ceylon JARs and CARs
        String version = LauncherUtil.determineSystemVersion();
        archives.add(getRepoCar(ceylonRepo, "ceylon.language", version));
        archives.add(getRepoJar(ceylonRepo, "ceylon.runtime", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.common", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.model", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.typechecker", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.compiler.java", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.compiler.js", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.cli", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.tool.provider", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.tools", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.langtools.classfile", version));
        
        //CMR
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.module-loader", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.module-resolver", version));
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.module-resolver-aether", version)); // optional
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.module-resolver-webdav", version)); // optional
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.module-resolver-javascript", version)); // optional
        
        //JBoss Modules
        archives.add(getRepoJar(ceylonRepo, "org.jboss.modules", Versions.DEPENDENCY_JBOSS_MODULES_VERSION));
        archives.add(getRepoJar(ceylonRepo, "org.jboss.logmanager", Versions.DEPENDENCY_LOGMANAGER_VERSION));
        
        // Maven, HTTP, and WebDAV support used by CMR
        archives.add(getRepoJar(ceylonRepo, "org.eclipse.ceylon.aether", "3.3.9")); // optional

        // For the typechecker
        archives.add(getRepoJar(ceylonRepo, "org.antlr.runtime", "3.5.2"));
        // For the JS backend
        archives.add(getRepoJar(ceylonRepo, "net.minidev.json-smart", "1.3.1"));
        // For the "doc" tool
        archives.add(getRepoJar(ceylonRepo, "org.tautua.markdownpapers.core", "1.3.4"));
        archives.add(getRepoJar(ceylonRepo, "com.github.rjeschke.txtmark", "0.13"));
        
        return archives;
    }

    private static File getRepoJar(File repo, String moduleName, String version) {
        return getRepoUrl(repo, moduleName, version, "jar");
    }

    private static File getRepoCar(File repo, String moduleName, String version) {
        return getRepoUrl(repo, moduleName, version, "car");
    }

    private static File getRepoUrl(File repo, String moduleName, String version, String extension) {
        return new File(repo, moduleName.replace('.', '/') + "/" + version + "/" + moduleName + "-" + version + "." + extension);
    }

    public static File getRepoJar(String moduleName, String version) throws FileNotFoundException, URISyntaxException {
        return getRepoUrl(moduleName, version, "jar");
    }

    public static File getRepoCar(String moduleName, String version) throws FileNotFoundException, URISyntaxException {
        return getRepoUrl(moduleName, version, "car");
    }

    public static File getRepoUrl(String moduleName, String version, String extension) throws URISyntaxException, FileNotFoundException {
        // Determine the necessary folders
        File ceylonHome = LauncherUtil.determineHome();
        File ceylonRepo = LauncherUtil.determineRepo(ceylonHome);

        // Perform some sanity checks
        checkFolders(ceylonHome, ceylonRepo);
        
        return new File(ceylonRepo, moduleName.replace('.', '/') + "/" + version + "/" + moduleName + "-" + version + "." + extension);
    }

    private static void checkFolders(File ceylonHome, File ceylonRepo) throws FileNotFoundException {
        if (!ceylonHome.isDirectory()) {
            throw new FileNotFoundException("Could not determine the Ceylon home directory (" + ceylonHome + ")");
        }
        if (!ceylonRepo.isDirectory()) {
            throw new FileNotFoundException("The Ceylon system repository could not be found (" + ceylonRepo + ")");
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // checking local
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // checking parent
                // This call to loadClass may eventually call findClass again, in case the parent doesn't find anything.
                c = super.loadClass(name, resolve);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            // This call to getResource may eventually call findResource again, in case the parent doesn't find anything.
            url = super.getResource(name);
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        /**
        * Similar to super, but local resources are enumerated before parent resources
        */
        Enumeration<URL> localUrls = findResources(name);
        Enumeration<URL> parentUrls = null;
        if (getParent() != null) {
            parentUrls = getParent().getResources(name);
        }
        final List<URL> urls = new ArrayList<URL>();
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                urls.add(localUrls.nextElement());
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement());
            }
        }
        return Collections.enumeration(urls);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        if (url != null) {
            try {
                URLConnection con = url.openConnection();
                con.setUseCaches(false);
                return con.getInputStream();
            } catch (IOException e) {
            }
        }
        return null;
    }
}
